package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.model.APIParam;
import org.example.model.Pokemon;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class APIService {
    private static final APIService instance = new APIService();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static APIService getInstance() {
        return instance;
    }

    private APIService() {

    }


    public String callAPI(APIParam apiParam) throws Exception {
        String myurl = "https://pokeapi.co/api/v2/pokemon/%s".formatted(apiParam.my());
        String otherurl = "https://pokeapi.co/api/v2/pokemon/%s".formatted(apiParam.other());

        HttpRequest myrequest = HttpRequest.newBuilder()
                .uri(URI.create(myurl))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> myresponse = httpClient.send(myrequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest otherrequest = HttpRequest.newBuilder()
                .uri(URI.create(otherurl))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> otherresponse = httpClient.send(otherrequest, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();

        String myBody = myresponse.body();
        Pokemon myPokemon = objectMapper.readValue(myBody, Pokemon.class);

        String otherBody = otherresponse.body();
        Pokemon otherPokemon = objectMapper.readValue(otherBody, Pokemon.class);

        Map<String, Object> map = new HashMap<>();

        map.put("my_name", myPokemon.name);
        map.put("my_image", myPokemon.sprites.backDefault);
        
        Pokemon.Stats myStats = myPokemon.getStatsMap();
        map.put("my_hp", myStats.getHp());
        map.put("my_attack", myStats.getAttack());
        map.put("my_defense", myStats.getDefense());
        map.put("my_special_attack", myStats.getSpecialAttack());
        map.put("my_special_defense", myStats.getSpecialDefense());
        map.put("my_speed", myStats.getSpeed());

        List<Pokemon.MoveWrapper> shuffledmyMoves = new ArrayList<>(myPokemon.moves);
        Collections.shuffle(shuffledmyMoves);

        for (int i = 1; i<5; i++) {
            map.put("my_skill%s".formatted(Integer.toString(i)), shuffledmyMoves.get(i).move.name);
        }
        // 상대 포켓몬 정보
        map.put("other_name", otherPokemon.name);
        map.put("other_image", otherPokemon.sprites.frontDefault);

        Pokemon.Stats otherStats = otherPokemon.getStatsMap();
        map.put("other_hp", otherStats.getHp());
        map.put("other_attack", otherStats.getAttack());
        map.put("other_defense", otherStats.getDefense());
        map.put("other_special_attack", otherStats.getSpecialAttack());
        map.put("other_special_defense", otherStats.getSpecialDefense());
        map.put("other_speed", otherStats.getSpeed());

        List<Pokemon.MoveWrapper> shuffledMoves = new ArrayList<>(otherPokemon.moves);
        Collections.shuffle(shuffledMoves);

        for (int i = 1; i<5; i++) {
            map.put("other_skill%s".formatted(Integer.toString(i)), shuffledMoves.get(i).move.name);
        }

        // JSON 변환하여 반환
        return objectMapper.writeValueAsString(map);
    }
}

