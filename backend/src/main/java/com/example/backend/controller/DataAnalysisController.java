package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/analysis")
public class DataAnalysisController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/overview")
    public Map<String, Object> getOverview(@RequestParam(required = false) String category) {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            String whereClause = "";
            if (category != null && !category.isEmpty() && !category.equals("全部")) {
                whereClause = " WHERE category = '" + category.replace("'", "''") + "'";
            }
            
            long totalVideos = getCount(conn, "SELECT COUNT(*) FROM bilibili_video" + whereClause);
            long totalViewCount = getSum(conn, "SELECT COALESCE(SUM(view_count), 0) FROM bilibili_video" + whereClause);
            long totalLikeCount = getSum(conn, "SELECT COALESCE(SUM(like_count), 0) FROM bilibili_video" + whereClause);
            long totalCoinCount = getSum(conn, "SELECT COALESCE(SUM(coin_count), 0) FROM bilibili_video" + whereClause);
            long totalFavoriteCount = getSum(conn, "SELECT COALESCE(SUM(favorite_count), 0) FROM bilibili_video" + whereClause);
            long totalDanmakuCount = getSum(conn, "SELECT COALESCE(SUM(danmaku_count), 0) FROM bilibili_video" + whereClause);
            long totalShareCount = getSum(conn, "SELECT COALESCE(SUM(share_count), 0) FROM bilibili_video" + whereClause);
            long totalReplyCount = getSum(conn, "SELECT COALESCE(SUM(reply_count), 0) FROM bilibili_video" + whereClause);
            
            result.put("totalVideos", totalVideos);
            result.put("totalViewCount", totalViewCount);
            result.put("totalLikeCount", totalLikeCount);
            result.put("totalCoinCount", totalCoinCount);
            result.put("totalFavoriteCount", totalFavoriteCount);
            result.put("totalDanmakuCount", totalDanmakuCount);
            result.put("totalShareCount", totalShareCount);
            result.put("totalReplyCount", totalReplyCount);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/category-distribution")
    public Map<String, Object> getCategoryDistribution() {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            List<Map<String, Object>> data = new ArrayList<>();
            String sql = "SELECT COALESCE(category, '未分类') as category, COUNT(*) as count " +
                        "FROM bilibili_video GROUP BY category ORDER BY count DESC LIMIT 20";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("category", rs.getString("category"));
                    item.put("count", rs.getLong("count"));
                    data.add(item);
                }
            }
            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/top-videos")
    public Map<String, Object> getTopVideos(@RequestParam(defaultValue = "view") String sortBy) {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            List<Map<String, Object>> data = new ArrayList<>();
            String orderColumn = "view_count";
            if ("like".equals(sortBy)) orderColumn = "like_count";
            else if ("coin".equals(sortBy)) orderColumn = "coin_count";
            else if ("favorite".equals(sortBy)) orderColumn = "favorite_count";
            else if ("danmaku".equals(sortBy)) orderColumn = "danmaku_count";
            else if ("share".equals(sortBy)) orderColumn = "share_count";
            else if ("reply".equals(sortBy)) orderColumn = "reply_count";
            
            String sql = String.format(
                "SELECT bvid, title, view_count, like_count, coin_count, favorite_count, danmaku_count, share_count, reply_count, category " +
                "FROM bilibili_video ORDER BY %s DESC LIMIT 20", orderColumn);
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("bvid", rs.getString("bvid"));
                    item.put("title", rs.getString("title"));
                    item.put("viewCount", rs.getLong("view_count"));
                    item.put("likeCount", rs.getLong("like_count"));
                    item.put("coinCount", rs.getLong("coin_count"));
                    item.put("favoriteCount", rs.getLong("favorite_count"));
                    item.put("danmakuCount", rs.getLong("danmaku_count"));
                    item.put("shareCount", rs.getLong("share_count"));
                    item.put("replyCount", rs.getLong("reply_count"));
                    item.put("category", rs.getString("category"));
                    data.add(item);
                }
            }
            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/tag-cloud")
    public Map<String, Object> getTagCloud(@RequestParam(required = false) String category) {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            Map<String, Integer> tagCount = new HashMap<>();
            String sql;
            PreparedStatement ps;
            if (category != null && !category.isEmpty() && !category.equals("全部")) {
                sql = "SELECT tags FROM bilibili_video WHERE tags IS NOT NULL AND tags != '' AND category = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, category);
            } else {
                sql = "SELECT tags FROM bilibili_video WHERE tags IS NOT NULL AND tags != ''";
                ps = conn.prepareStatement(sql);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tagsStr = rs.getString("tags");
                    if (tagsStr != null && !tagsStr.isEmpty()) {
                        try {
                            String[] tags = tagsStr.replace("[", "").replace("]", "")
                                    .replace("\"", "").split(",");
                            for (String tag : tags) {
                                tag = tag.trim();
                                if (!tag.isEmpty()) {
                                    tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
            ps.close();
            List<Map<String, Object>> data = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : tagCount.entrySet()) {
                if (entry.getValue() >= 2) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    data.add(item);
                }
            }
            data.sort((a, b) -> (Integer) b.get("value") - (Integer) a.get("value"));
            if (data.size() > 100) data = data.subList(0, 100);
            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/interaction-stats")
    public Map<String, Object> getInteractionStats() {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            List<Map<String, Object>> data = new ArrayList<>();
            String sql = "SELECT " +
                "AVG(like_count) as avgLike, " +
                "AVG(coin_count) as avgCoin, " +
                "AVG(favorite_count) as avgFavorite, " +
                "AVG(danmaku_count) as avgDanmaku, " +
                "AVG(share_count) as avgShare, " +
                "AVG(reply_count) as avgReply, " +
                "MAX(like_count) as maxLike, " +
                "MAX(coin_count) as maxCoin, " +
                "MAX(favorite_count) as maxFavorite " +
                "FROM bilibili_video";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("avgLike", rs.getDouble("avgLike"));
                    item.put("avgCoin", rs.getDouble("avgCoin"));
                    item.put("avgFavorite", rs.getDouble("avgFavorite"));
                    item.put("avgDanmaku", rs.getDouble("avgDanmaku"));
                    item.put("avgShare", rs.getDouble("avgShare"));
                    item.put("avgReply", rs.getDouble("avgReply"));
                    item.put("maxLike", rs.getLong("maxLike"));
                    item.put("maxCoin", rs.getLong("maxCoin"));
                    item.put("maxFavorite", rs.getLong("maxFavorite"));
                    data.add(item);
                }
            }
            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/daily-trend")
    public Map<String, Object> getDailyTrend() {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            List<Map<String, Object>> data = new ArrayList<>();
            String sql = "SELECT DATE(create_time) as date, COUNT(*) as count " +
                        "FROM bilibili_video " +
                        "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                        "GROUP BY DATE(create_time) ORDER BY date";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", rs.getString("date"));
                    item.put("count", rs.getLong("count"));
                    data.add(item);
                }
            }
            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private long getCount(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    @GetMapping("/category-likes")
    public Map<String, Object> getCategoryLikes() {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            List<Map<String, Object>> data = new ArrayList<>();
            String sql = "SELECT COALESCE(category, '未分类') as category, " +
                        "COALESCE(SUM(like_count), 0) as totalLikes " +
                        "FROM bilibili_video " +
                        "GROUP BY category " +
                        "ORDER BY totalLikes DESC " +
                        "LIMIT 15";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("category", rs.getString("category"));
                    item.put("totalLikes", rs.getLong("totalLikes"));
                    data.add(item);
                }
            }
            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/data-completeness")
    public Map<String, Object> getDataCompleteness() {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            long totalVideos = getCount(conn, "SELECT COUNT(*) FROM bilibili_video");
            
            long duplicateBvids = getCount(conn, 
                "SELECT COUNT(*) FROM (SELECT bvid FROM bilibili_video GROUP BY bvid HAVING COUNT(*) > 1) AS t");
            
            long nullBvids = getCount(conn, 
                "SELECT COUNT(*) FROM bilibili_video WHERE bvid IS NULL OR bvid = ''");
            long nullTitles = getCount(conn, 
                "SELECT COUNT(*) FROM bilibili_video WHERE title IS NULL OR title = ''");
            long nullCategories = getCount(conn, 
                "SELECT COUNT(*) FROM bilibili_video WHERE category IS NULL OR category = ''");
            long nullViewCount = getCount(conn, 
                "SELECT COUNT(*) FROM bilibili_video WHERE view_count IS NULL");
            long nullLikeCount = getCount(conn, 
                "SELECT COUNT(*) FROM bilibili_video WHERE like_count IS NULL");
            
            long totalFields = totalVideos * 6;
            long missingFields = nullBvids + nullTitles + nullCategories + nullViewCount + nullLikeCount;
            long duplicatePenalty = duplicateBvids * 2;
            
            long validRecords = totalVideos - duplicateBvids;
            long completeFields = totalFields - missingFields - duplicatePenalty;
            
            double completeness = totalFields > 0 ? (completeFields * 100.0 / totalFields) : 100.0;
            completeness = Math.max(0, Math.min(100, completeness));
            
            result.put("totalVideos", totalVideos);
            result.put("duplicateBvids", duplicateBvids);
            result.put("nullBvids", nullBvids);
            result.put("nullTitles", nullTitles);
            result.put("nullCategories", nullCategories);
            result.put("completeness", Math.round(completeness * 100) / 100.0);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private long getSum(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }
}
