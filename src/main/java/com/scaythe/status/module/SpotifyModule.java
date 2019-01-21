package com.scaythe.status.module;

import com.scaythe.status.input.ClickEvent;
import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusMap;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.mpris.MediaPlayer2.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpotifyModule extends StatusModule {

    private static final String NAME = "spotify";

    private static final String DBUS_BUS_NAME = "org.freedesktop.DBus";
    private static final String DBUS_PATH = "/org/freedesktop/DBus";
    private static final String SPOTIFY_BUS_NAME = "org.mpris.MediaPlayer2.spotify";
    private static final String SPOTIFY_PATH = "/org/mpris/MediaPlayer2";

    private DBusConnection connection = null;
    private Player player = null;

    public SpotifyModule(Runnable update) {
        this(null, update);
    }

    public SpotifyModule(String instance, Runnable update) {
        super(NAME, instance, null, update);
    }

    @Override
    public void start() {
        if (connection != null) {
            return;
        }

        String address = System.getenv("DBUS_SESSION_BUS_ADDRESS");

        try {
            connection = DBusConnection.getConnection(address);
        } catch (DBusException e) {
            return;
        }

        try {
            DBus dbus = connection.getRemoteObject(DBUS_BUS_NAME, DBUS_PATH, DBus.class);

            registerSpotifyStartStopListener();

            if (dbus.NameHasOwner(SPOTIFY_BUS_NAME)) {
                String spotifyName = dbus.GetNameOwner(SPOTIFY_BUS_NAME);

                spotifyRunning(spotifyName);
            }
        } catch (DBusException e) {
            e.printStackTrace();

            stop();
        }
    }

    private void registerSpotifyStartStopListener() throws DBusException {
        connection.addSigHandler(DBus.NameOwnerChanged.class, this::nameOwnerChanged);
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
            e.printStackTrace();
        }
    }

    private void spotifyStopping(String spotifyName) throws DBusException {
        player = null;

        connection.removeSigHandler(Properties.PropertiesChanged.class, spotifyName, this::propertiesChanged);
        update(new ModuleData(""));
    }

    private void spotifyRunning(String spotifyName) throws DBusException {
        connection.addSigHandler(Properties.PropertiesChanged.class, spotifyName, this::propertiesChanged);
        currentStatus();

        player = connection.getPeerRemoteObject(SPOTIFY_BUS_NAME, SPOTIFY_PATH, Player.class);
    }

    private void currentStatus() throws DBusException {
        Properties props = connection.getRemoteObject(SPOTIFY_BUS_NAME,
                SPOTIFY_PATH,
                Properties.class);

        update(props.GetAll(Player.class.getName()));
    }

    private void propertiesChanged(Properties.PropertiesChanged p) {
        update(p.getPropertiesChanged());
    }

    private void update(Map<String, Variant<?>> map) {
        Map<String, String> metadata = metadata(map);

        String song = metadata.get("xesam:title");
        String artist = metadata.get("xesam:artist");

        String text = song + " - " + artist;

        update(playbackStatus(map).flatMap(this::color)
                .map(c -> new ModuleData(text, c))
                .orElseGet(() -> new ModuleData(text)));
    }

    private Optional<String> color(String status) {
        switch (status) {
            case "Playing":
                return Optional.of("#00ff00");
            case "Paused":
                return Optional.of("#ffff00");
            default:
                return Optional.of("#ff0000");
        }
    }

    private Map<String, String> metadata(Map<String, Variant<?>> map) {
        return Optional.ofNullable(map.get("Metadata"))
                .filter(v -> v.getType().getTypeName().startsWith("org.freedesktop.dbus.types.DBusMapType"))
                .map(Variant::getValue)
                .map(DBusMap.class::cast)
                .map(this::toMap)
                .orElse(Collections.emptyMap());
    }

    private Map<String, String> toMap(DBusMap<String, Variant<?>> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> variant(e.getValue())));
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

    @Override
    public void event(ClickEvent event) {
        if (player != null) {
            player.PlayPause();
        }
    }

    @Override
    public void stop() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Override
    public boolean isRunning() {
        return connection != null;
    }
}
