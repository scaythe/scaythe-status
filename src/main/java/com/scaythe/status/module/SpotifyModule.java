package com.scaythe.status.module;

import static java.util.stream.Collectors.toMap;

import com.scaythe.status.input.ClickEvent;
import com.scaythe.status.module.config.ModuleConfig;
import com.scaythe.status.write.ModuleData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.DBusMap;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.jspecify.annotations.Nullable;
import org.mpris.MediaPlayer2.Player;

@Slf4j
public class SpotifyModule extends Module {
  private static final String DBUS_BUS_NAME = "org.freedesktop.DBus";
  private static final String DBUS_PATH = "/org/freedesktop/DBus";
  private static final String SPOTIFY_BUS_NAME = "org.mpris.MediaPlayer2.spotify";
  private static final String SPOTIFY_PATH = "/org/mpris/MediaPlayer2";

  private @Nullable SpotifyConnection connection;

  public SpotifyModule(ModuleConfig config, Consumer<ModuleData> output) {
    super(config, output);
  }

  @Override
  public String defaultName() {
    return "spotify";
  }

  private class SpotifyConnection {
    private final DBusConnection connection;
    private @Nullable Player player = null;

    private SpotifyConnection(DBusConnection connection) throws DBusException {
      this.connection = connection;

      DBus dbus = connection.getRemoteObject(DBUS_BUS_NAME, DBUS_PATH, DBus.class);

      registerSpotifyStartStopListener();

      startupStatus(dbus);
    }

    private void registerSpotifyStartStopListener() throws DBusException {
      connection.addSigHandler(DBus.NameOwnerChanged.class, this::nameOwnerChanged);
    }

    private void startupStatus(DBus dbus) {
      if (dbus.NameHasOwner(SPOTIFY_BUS_NAME)) {
        String spotifyName = dbus.GetNameOwner(SPOTIFY_BUS_NAME);

        spotifyRunning(spotifyName);
      }
    }

    private void nameOwnerChanged(DBus.NameOwnerChanged noc) {
      String name = noc.name;

      if (!name.equals(SPOTIFY_BUS_NAME)) {
        return;
      }

      String oldOwner = noc.oldOwner;
      String newOwner = noc.newOwner;

      try {
        if (!newOwner.isEmpty() && oldOwner.isEmpty()) {
          spotifyRunning(newOwner);
        }

        if (newOwner.isEmpty() && !oldOwner.isEmpty()) {
          spotifyStopping(oldOwner);
        }
      } catch (DBusException e) {
        log.atError().setCause(e).log("error handling name owner changed");
      }
    }

    private void spotifyStopping(String spotifyName) throws DBusException {
      player = null;

      connection.removeSigHandler(
          Properties.PropertiesChanged.class, spotifyName, this::propertiesChanged);

      ModuleData data = ModuleData.empty(name());

      output(data);
    }

    private void spotifyRunning(String spotifyName) {
      try {
        connection.addSigHandler(
            Properties.PropertiesChanged.class, spotifyName, this::propertiesChanged);
        currentStatus();

        player = connection.getPeerRemoteObject(SPOTIFY_BUS_NAME, SPOTIFY_PATH, Player.class);
      } catch (DBusException e) {
        log.atError().setCause(e).log("error handling spotify running");
      }
    }

    private void currentStatus() {
      try {
        Properties props =
            connection.getRemoteObject(SPOTIFY_BUS_NAME, SPOTIFY_PATH, Properties.class);

        update(props.GetAll(Player.class.getName()));
      } catch (DBusException e) {
        log.atError().setCause(e).log("error getting current status");
      }
    }

    private void propertiesChanged(Properties.PropertiesChanged p) {
      update(p.getPropertiesChanged());
    }

    private void update(Map<String, Variant<?>> map) {
      Map<String, String> metadata = metadata(map);

      String song = metadata.get("xesam:title");
      String artist = metadata.get("xesam:artist");

      String text = song + " - " + artist;

      ModuleData data =
          playbackStatus(map)
              .flatMap(this::color)
              .map(c -> ModuleData.ofColor(text, c, name()))
              .orElseGet(() -> ModuleData.of(text, name()));

      output(data);
    }

    private Optional<String> color(String status) {
      return switch (status) {
        case "Playing" -> Optional.of("#00ff00");
        case "Paused" -> Optional.of("#ffff00");
        default -> Optional.of("#ff0000");
      };
    }

    private Map<String, String> metadata(Map<String, Variant<?>> map) {
      return Optional.ofNullable(map.get("Metadata"))
          .filter(
              v -> v.getType().getTypeName().startsWith("org.freedesktop.dbus.types.DBusMapType"))
          .map(Variant::getValue)
          .map(DBusMap.class::cast)
          .map(this::asMap)
          .orElse(Collections.emptyMap());
    }

    private Map<String, String> asMap(DBusMap<String, Variant<?>> map) {
      return map.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> variant(e.getValue())));
    }

    private Optional<String> playbackStatus(Map<String, Variant<?>> map) {
      return Optional.ofNullable(map.get("PlaybackStatus")).map(this::variant);
    }

    private String variant(Variant<?> v) {
      if (v.getSig().equals("as")) {
        return variantStringList((Variant<List<String>>) v);
      }

      return v.getValue().toString();
    }

    private String variantStringList(Variant<List<String>> v) {
      return String.join(", ", v.getValue());
    }

    public void disconnect() {
      connection.disconnect();
    }

    public void event() {
      if (player != null) {
        player.PlayPause();
      }
    }
  }

  @Override
  public void event(ClickEvent event) {
    if (connection == null) return;

    connection.event();
  }

  @Override
  public synchronized void start() {
    if (connection != null) throw new IllegalStateException("already running");

    String address = System.getenv("DBUS_SESSION_BUS_ADDRESS");

    try {
      DBusConnection dBusConnection = DBusConnection.getConnection(address);
      this.connection = new SpotifyConnection(dBusConnection);
    } catch (DBusException e) {
      stop();

      throw new IllegalStateException("dbus connection failed", e);
    }
  }

  @Override
  public synchronized void stop() {
    if (connection == null) return;

    connection.disconnect();
  }
}
