package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
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

        Pokemon.Stats myStats = myPokemon.getStatsMap();
        map.put("my_name", myPokemon.name);
        map.put("my_image", myPokemon.sprites.backDefault);
        map.put("my_type", myPokemon.types.get(0).type.name);
        if(myPokemon.types.size() > 1) {
            map.put("my_type2", myPokemon.types.get(1).type.name);
        }


        map.put("my_hp", myStats.getHp());
        map.put("my_attack", myStats.getAttack());
        map.put("my_defense", myStats.getDefense());
        map.put("my_special_attack", myStats.getSpecialAttack());
        map.put("my_special_defense", myStats.getSpecialDefense());
        map.put("my_speed", myStats.getSpeed());

        List<Pokemon.MoveWrapper> shuffledmyMoves = new ArrayList<>(myPokemon.moves);
        Collections.shuffle(shuffledmyMoves);

        int validCount = 0;
        int i = 0;
        while (validCount < 4 && i < shuffledmyMoves.size()){
            String skillName = shuffledmyMoves.get(i).move.name;
            String skillUrl = shuffledmyMoves.get(i).move.url;

            // 기술 상세 정보 요청
            HttpRequest moveRequest = HttpRequest.newBuilder()
                    .uri(URI.create(skillUrl))
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> moveResponse = httpClient.send(moveRequest, HttpResponse.BodyHandlers.ofString());

            // 기술 상세 정보 파싱
            JsonNode moveData = objectMapper.readTree(moveResponse.body());

            int power = moveData.get("power").asInt();

            if (power == 0) {
                i++;
                continue;
            }

            // 기본 정보는 반드시 포함
            map.put("my_skill" + (validCount+1) + "_name", skillName);

            // 나머지 정보는 null 체크 후 포함
            if (!moveData.get("power").isNull()) {
                map.put("my_skill" + (validCount+1) + "_power", moveData.get("power").asInt());
            } else {
                map.put("my_skill" + (validCount+1) + "_power", 0); // 상태 변화 기술은 0으로 설정
            }

            if (!moveData.get("accuracy").isNull()) {
                map.put("my_skill" + (validCount+1) + "_accuracy", moveData.get("accuracy").asInt());
            } else {
                map.put("my_skill" + (validCount+1) + "_accuracy", 100); // 무조건 명중하는 기술
            }

            map.put("my_skill" + (validCount+1) + "_pp", moveData.get("pp").asInt());
            map.put("my_skill" + (validCount+1) + "_type", moveData.get("type").get("name").asText());
            validCount ++;
            i++;
        }
        // 상대 포켓몬 정보
        map.put("other_name", otherPokemon.name);
        map.put("other_image", otherPokemon.sprites.frontDefault);
        if(otherPokemon.types.size() > 1) {
            map.put("other_type2", otherPokemon.types.get(1).type.name);
        }

        Pokemon.Stats otherStats = otherPokemon.getStatsMap();
        map.put("other_hp", otherStats.getHp());
        map.put("other_attack", otherStats.getAttack());
        map.put("other_defense", otherStats.getDefense());
        map.put("other_special_attack", otherStats.getSpecialAttack());
        map.put("other_special_defense", otherStats.getSpecialDefense());
        map.put("other_speed", otherStats.getSpeed());

        List<Pokemon.MoveWrapper> shuffledMoves = new ArrayList<>(otherPokemon.moves);
        Collections.shuffle(shuffledMoves);

        validCount = 0;
        i = 0;
        while (validCount < 4 && i < shuffledmyMoves.size()){
            String skillName = shuffledMoves.get(i).move.name;
            String skillUrl = shuffledMoves.get(i).move.url;

            // 기술 상세 정보 요청
            HttpRequest moveRequest = HttpRequest.newBuilder()
                    .uri(URI.create(skillUrl))
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> moveResponse = httpClient.send(moveRequest, HttpResponse.BodyHandlers.ofString());

            // 기술 상세 정보 파싱
            JsonNode moveData = objectMapper.readTree(moveResponse.body());

            int power = moveData.get("power").asInt();

            if (power == 0) {
                i++;
                continue;
            }

            // 기본 정보는 반드시 포함
            map.put("other_skill" + (validCount+1) + "_name", skillName);

            // 나머지 정보는 null 체크 후 포함
            if (!moveData.get("power").isNull()) {
                map.put("other_skill" + (validCount+1) + "_power", moveData.get("power").asInt());
            } else {
                map.put("other_skill" + (validCount+1) + "_power", 0); // 상태 변화 기술은 0으로 설정
            }

            if (!moveData.get("accuracy").isNull()) {
                map.put("other_skill" + (validCount+1) + "_accuracy", moveData.get("accuracy").asInt());
            } else {
                map.put("other_skill" + (validCount+1) + "_accuracy", 100); // 무조건 명중하는 기술
            }

            map.put("other_skill" + (validCount+1) + "_pp", moveData.get("pp").asInt());
            map.put("other_skill" + (validCount+1) + "_type", moveData.get("type").get("name").asText());
            validCount ++;
            i++;
        }

        System.out.println("map = " + map);
        // JSON 변환하여 반환
        return objectMapper.writeValueAsString(map);
    }
}

