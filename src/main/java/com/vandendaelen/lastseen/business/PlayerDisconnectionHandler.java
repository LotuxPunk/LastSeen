package com.vandendaelen.lastseen.business;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PlayerDisconnectionHandler {
    private static volatile PlayerDisconnectionHandler INSTANCE;
    private HashMap<String, LocalDateTime> players;

    public PlayerDisconnectionHandler() {
        this.players = new HashMap<>();
        readJson();
    }

    public static PlayerDisconnectionHandler getInstance(){
        final PlayerDisconnectionHandler result = INSTANCE;
        if (result != null){
            return result;
        }
        synchronized (PlayerDisconnectionHandler.class){
            if (INSTANCE == null){
                INSTANCE = new PlayerDisconnectionHandler();
            }
            return INSTANCE;
        }
    }

    public void addPlayerDisconnection(ServerPlayerEntity playerEntity, LocalDateTime date){
        if (players.containsKey(playerEntity.getName().asString())){
            players.replace(playerEntity.getName().asString(), date);
        }
        else {
            players.put(playerEntity.getName().asString(), date);
        }

        final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        final String prettyJson = prettyGson.toJson(players);

        writeJson(prettyJson);
    }

    public String getPlayerLastTime(MinecraftServer server, String username){
        if (Arrays.asList(server.getPlayerNames()).contains(username)){
            return "now";
        }
        else {
            if (players.containsKey(username)){
                final LocalDateTime today = LocalDateTime.now();
                final LocalDateTime lastDisconnection = players.get(username);
                final long daysBetween = ChronoUnit.DAYS.between(lastDisconnection, today);
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                final String formatDateTime = lastDisconnection.format(formatter);

                return MessageFormat.format("{0} - ({1} day(s))", formatDateTime, daysBetween);
            }
            return  null;
        }
    }

    private void writeJson(String json) {
        FileWriter file = null;
        try {
            file = new FileWriter("lastseen.json");
            file.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void readJson() {
        final Gson gson = new Gson();
        final File json = new File("lastseen.json");
        try {
            Type type = new TypeToken<HashMap<String, LocalDateTime>>(){}.getType();
            if (Files.exists(json.toPath())){
                Reader reader = Files.newBufferedReader(json.toPath());
                players = gson.fromJson(reader, type);
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getUsernameList() {
        return new ArrayList<>(players.keySet());
    }
}
