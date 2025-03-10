package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pokemon{

    public List<MoveWrapper> moves; // moves는 List 형태로 선언해야 함

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MoveWrapper {
        public Move move;  // 실제 move 데이터를 감싸는 클래스
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Move {
        public String name;
        public String url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true) // 추가
    public static class Sprites {
        @JsonProperty("front_default")
        public String frontDefault;

        @JsonProperty("back_default")
        public String backDefault;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatDetail {
        public String name;
        public String url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatItem {
        @JsonProperty("base_stat")
        public int baseStat;

        @JsonProperty("effort")
        public int effort;

        @JsonProperty("stat")
        public StatDetail stat;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {
        private Map<String, Integer> statMap = new HashMap<>();

        // Jackson이 사용할 빈 생성자
        public Stats() {}

        // 기존 stats 배열을 내부 맵으로 변환하는 메서드
        @JsonProperty("stats")
        private void setStats(List<StatItem> stats) {
            for (StatItem item : stats) {
                statMap.put(item.stat.name, item.baseStat);
            }
        }

        // 편리한 접근을 위한 getter 메서드들
        public int getHp() {
            return statMap.getOrDefault("hp", 0);
        }

        public int getAttack() {
            return statMap.getOrDefault("attack", 0);
        }

        public int getDefense() {
            return statMap.getOrDefault("defense", 0);
        }

        public int getSpecialAttack() {
            return statMap.getOrDefault("special-attack", 0);
        }

        public int getSpecialDefense() {
            return statMap.getOrDefault("special-defense", 0);
        }

        public int getSpeed() {
            return statMap.getOrDefault("speed", 0);
        }
    }

    public Sprites sprites;
    public String name;
    public List<StatItem> stats;

    // Stats 객체를 통해 편리하게 능력치 접근
    public Stats getStatsMap() {
        Stats statsMap = new Stats();
        statsMap.setStats(this.stats);
        return statsMap;
    }



}



