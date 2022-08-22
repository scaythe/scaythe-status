package org.mpris.MediaPlayer2;

import org.freedesktop.dbus.interfaces.DBusInterface;

public interface Player extends DBusInterface {

  void PlayPause();
}
