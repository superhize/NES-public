package be.hize.nes.config;

import be.hize.nes.features.misc.waypoint.Waypoint;
import com.google.gson.annotations.Expose;

import java.util.*;

public class Storage {

    @Expose
    public Map<UUID, PlayerSpecific> players = new HashMap<>();
    public static class PlayerSpecific {
        @Expose
        public Map<String, ProfileSpecific> profiles = new HashMap<>(); // profile name
    }

    public static class ProfileSpecific {

        @Expose
        public List<Waypoint.Waypoints> waypoints = new LinkedList<>();
    }
}
