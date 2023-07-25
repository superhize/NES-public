package be.hize.nes.config;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Storage {

    @Expose
    public Map<UUID, PlayerSpecific> players = new HashMap<>();
    public static class PlayerSpecific {
        @Expose
        public Map<String, ProfileSpecific> profiles = new HashMap<>(); // profile name
    }

    public static class ProfileSpecific {
    }
}
