package show.twilight.finale.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.Predicate;

/**
 * Per-face UV model; retains original Blockbench bone and animation coordinates.
 */
public final class BlockbenchBossModel {
    private final Map<String, JsonObject> groups = new HashMap<>(), cubes = new HashMap<>(), animations = new HashMap<>();
    private final Map<String, JsonObject> trees = new HashMap<>();
    private final Map<String, String> parents = new HashMap<>();
    private final JsonArray roots;
    private final float texWidth, texHeight;
    private Predicate<String> filter = name -> true;

    public BlockbenchBossModel(ResourceLocation location) {
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(location)) {
            JsonObject model = JsonParser.parseReader(reader).getAsJsonObject();
            for (JsonElement entry : model.getAsJsonArray("groups")) {
                JsonObject g = entry.getAsJsonObject();
                groups.put(g.get("uuid").getAsString(), g);
            }
            for (JsonElement entry : model.getAsJsonArray("elements")) {
                JsonObject c = entry.getAsJsonObject();
                cubes.put(c.get("uuid").getAsString(), c);
            }
            for (JsonElement entry : model.getAsJsonArray("animations")) {
                JsonObject a = entry.getAsJsonObject();
                animations.put(a.get("name").getAsString(), a);
            }
            roots = model.getAsJsonArray("outliner");
            texWidth = model.getAsJsonObject("resolution").get("width").getAsFloat();
            texHeight = model.getAsJsonObject("resolution").get("height").getAsFloat();
            index(roots, null);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot load boss model: " + location, error);
        }
    }

    private void index(JsonArray nodes, String parent) {
        for (JsonElement node : nodes)
            if (node.isJsonObject()) {
                JsonObject tree = node.getAsJsonObject();
                String id = tree.get("uuid").getAsString();
                trees.put(id, tree);
                if (parent != null) parents.put(id, parent);
                index(tree.getAsJsonArray("children"), id);
            }
    }

    public void render(Map<String, Map<String, Vector3f>> animation, PoseStack pose, VertexConsumer vertex, int light, int color, Predicate<String> predicate) {
        filter = predicate;
        for (JsonElement root : roots) renderNode(root, new Vector3f(), animation, pose, vertex, light, color);
    }

    public void moveToBone(String name, Map<String, Map<String, Vector3f>> animation, PoseStack pose) {
        String id = groups.entrySet().stream().filter(e -> e.getValue().get("name").getAsString().equals(name)).map(Map.Entry::getKey).findFirst().orElseThrow();
        List<JsonObject> chain = new ArrayList<>();
        for (String current = id; current != null; current = parents.get(current)) chain.add(groups.get(current));
        Collections.reverse(chain);
        Vector3f parent = new Vector3f();
        for (JsonObject group : chain) {
            Vector3f origin = vector(group, "origin"), offset = new Vector3f(origin).sub(parent), rotation = vector(group, "rotation");
            var channels = animation.getOrDefault(group.get("name").getAsString(), Collections.emptyMap());
            offset.add(channels.getOrDefault("position", new Vector3f()));
            rotation.add(channels.getOrDefault("rotation", new Vector3f()));
            pose.translate(offset.x / 16, offset.y / 16, offset.z / 16);
            rotate(pose, rotation);
            Vector3f scale = channels.getOrDefault("scale", new Vector3f());
            pose.scale(1 + scale.x, 1 + scale.y, 1 + scale.z);
            parent = origin;
        }
    }

    public void renderContents(String bone, PoseStack pose, VertexConsumer vertex, int light, int color) {
        filter = name -> true;
        JsonObject group = groups.values().stream().filter(g -> g.get("name").getAsString().equals(bone)).findFirst().orElseThrow();
        JsonObject tree = trees.get(group.get("uuid").getAsString());
        for (JsonElement child : tree.getAsJsonArray("children"))
            renderNode(child, vector(group, "origin"), Collections.emptyMap(), pose, vertex, light, color);
    }

    public void sample(String name, float seconds, Map<String, Map<String, Vector3f>> result, float weight) {
        JsonObject animation = animations.get(name);
        if (animation == null || weight <= 0) return;
        float length = animation.get("length").getAsFloat();
        float time = animation.get("loop").getAsString().equals("loop") ? seconds % length : Mth.clamp(seconds, 0, length);
        for (var entry : animation.getAsJsonObject("animators").entrySet()) {
            JsonObject animator = entry.getValue().getAsJsonObject();
            if (!animator.has("keyframes") || !animator.has("name")) continue;
            Map<String, List<JsonObject>> channels = new HashMap<>();
            for (JsonElement element : animator.getAsJsonArray("keyframes")) {
                JsonObject key = element.getAsJsonObject();
                channels.computeIfAbsent(key.get("channel").getAsString(), k -> new ArrayList<>()).add(key);
            }
            for (var channel : channels.entrySet()) {
                List<JsonObject> keys = channel.getValue();
                keys.sort(Comparator.comparingDouble(k -> k.get("time").getAsDouble()));
                Vector3f value = interpolate(keys, time);
                if (channel.getKey().equals("scale")) value.sub(1, 1, 1);
                value.mul(weight);
                add(result, animator.get("name").getAsString(), channel.getKey(), value.x, value.y, value.z);
            }
        }
    }

    private static Vector3f interpolate(List<JsonObject> keys, float time) {
        int right = 0;
        while (right < keys.size() && keys.get(right).get("time").getAsFloat() < time) right++;
        if (right == 0) return point(keys.getFirst());
        if (right >= keys.size()) return point(keys.getLast());
        JsonObject a = keys.get(right - 1), b = keys.get(right);
        float f = (time - a.get("time").getAsFloat()) / (b.get("time").getAsFloat() - a.get("time").getAsFloat());
        if (b.get("interpolation").getAsString().equals("step")) return point(a);
        Vector3f p1 = point(a), p2 = point(b);
        if (!b.get("interpolation").getAsString().equals("catmullrom")) return p1.lerp(p2, f);
        Vector3f p0 = point(keys.get(Math.max(0, right - 2))), p3 = point(keys.get(Math.min(keys.size() - 1, right + 1)));
        Vector3f out = new Vector3f();
        for (int i = 0; i < 3; i++) {
            float v0 = p0.get(i), v1 = p1.get(i), v2 = p2.get(i), v3 = p3.get(i);
            out.setComponent(i, 0.5F * (2 * v1 + (-v0 + v2) * f + (2 * v0 - 5 * v1 + 4 * v2 - v3) * f * f + (-v0 + 3 * v1 - 3 * v2 + v3) * f * f * f));
        }
        return out;
    }

    private static Vector3f point(JsonObject key) {
        JsonObject p = key.getAsJsonArray("data_points").get(0).getAsJsonObject();
        return new Vector3f(p.get("x").getAsFloat(), p.get("y").getAsFloat(), p.get("z").getAsFloat());
    }

    private static void add(Map<String, Map<String, Vector3f>> map, String bone, String channel, float x, float y, float z) {
        map.computeIfAbsent(bone, k -> new HashMap<>()).computeIfAbsent(channel, k -> new Vector3f()).add(x, y, z);
    }

    private static void put(Map<String, Map<String, Vector3f>> map, String bone, String channel, Vector3f value) {
        map.computeIfAbsent(bone, k -> new HashMap<>()).put(channel, value);
    }

    private static Vector3f vector(JsonObject object, String name) {
        if (!object.has(name)) return new Vector3f();
        JsonArray a = object.getAsJsonArray(name);
        return new Vector3f(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
    }

    private static void rotate(PoseStack pose, Vector3f rotation) {
        pose.mulPose(Axis.ZP.rotationDegrees(rotation.z));
        pose.mulPose(Axis.YP.rotationDegrees(rotation.y));
        pose.mulPose(Axis.XP.rotationDegrees(rotation.x));
    }

    private void renderNode(JsonElement node, Vector3f parent, Map<String, Map<String, Vector3f>> animation, PoseStack pose, VertexConsumer vertex, int light, int color) {
        if (node.isJsonPrimitive()) {
            JsonObject cube = cubes.get(node.getAsString());
            if (cube != null && filter.test(cube.get("name").getAsString()))
                renderCube(cube, parent, pose, vertex, light, color);
            return;
        }
        JsonObject tree = node.getAsJsonObject(), group = groups.get(tree.get("uuid").getAsString());
        if (group == null) return;
        Vector3f origin = vector(group, "origin"), offset = new Vector3f(origin).sub(parent), rotation = vector(group, "rotation");
        var channels = animation.getOrDefault(group.get("name").getAsString(), Collections.emptyMap());
        offset.add(channels.getOrDefault("position", new Vector3f()));
        rotation.add(channels.getOrDefault("rotation", new Vector3f()));
        pose.pushPose();
        pose.translate(offset.x / 16, offset.y / 16, offset.z / 16);
        rotate(pose, rotation);
        Vector3f scale = channels.getOrDefault("scale", new Vector3f());
        pose.scale(1 + scale.x, 1 + scale.y, 1 + scale.z);
        for (JsonElement child : tree.getAsJsonArray("children"))
            renderNode(child, origin, animation, pose, vertex, light, color);
        pose.popPose();
    }

    private void renderCube(JsonObject cube, Vector3f parent, PoseStack pose, VertexConsumer vertex, int light, int color) {
        Vector3f from = vector(cube, "from"), to = vector(cube, "to"), pivot = vector(cube, "origin");
        pose.pushPose();
        Vector3f offset = new Vector3f(pivot).sub(parent);
        pose.translate(offset.x / 16, offset.y / 16, offset.z / 16);
        rotate(pose, vector(cube, "rotation"));
        from.sub(pivot).div(16);
        to.sub(pivot).div(16);
        float x = from.x, y = from.y, z = from.z, X = to.x, Y = to.y, Z = to.z;
        for (var face : cube.getAsJsonObject("faces").entrySet()) {
            JsonObject info = face.getValue().getAsJsonObject();
            if (!info.has("texture") || info.get("texture").isJsonNull()) continue;
            float[][] points;
            float nx = 0, ny = 0, nz = 0;
            switch (face.getKey()) {
                case "north" -> {
                    points = new float[][]{{X, Y, z}, {X, y, z}, {x, y, z}, {x, Y, z}};
                    nz = -1;
                }
                case "south" -> {
                    points = new float[][]{{x, Y, Z}, {x, y, Z}, {X, y, Z}, {X, Y, Z}};
                    nz = 1;
                }
                case "east" -> {
                    points = new float[][]{{X, Y, Z}, {X, y, Z}, {X, y, z}, {X, Y, z}};
                    nx = 1;
                }
                case "west" -> {
                    points = new float[][]{{x, Y, z}, {x, y, z}, {x, y, Z}, {x, Y, Z}};
                    nx = -1;
                }
                case "up" -> {
                    points = new float[][]{{x, Y, z}, {x, Y, Z}, {X, Y, Z}, {X, Y, z}};
                    ny = 1;
                }
                case "down" -> {
                    points = new float[][]{{x, y, Z}, {x, y, z}, {X, y, z}, {X, y, Z}};
                    ny = -1;
                }
                default -> {
                    continue;
                }
            }
            JsonArray uv = info.getAsJsonArray("uv");
            float u0 = uv.get(0).getAsFloat() / texWidth, v0 = uv.get(1).getAsFloat() / texHeight, u1 = uv.get(2).getAsFloat() / texWidth, v1 = uv.get(3).getAsFloat() / texHeight;
            float[][] uvs = {{u0, v0}, {u0, v1}, {u1, v1}, {u1, v0}};
            int shift = info.has("rotation") ? info.get("rotation").getAsInt() / 90 : 0;
            for (int i = 0; i < 4; i++) {
                float[] p = points[i], uvp = uvs[(i + shift) % 4];
                vertex.addVertex(pose.last(), p[0], p[1], p[2]).setColor(color).setUv(uvp[0], uvp[1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose.last(), nx, ny, nz);
            }
        }
        pose.popPose();
    }
}
