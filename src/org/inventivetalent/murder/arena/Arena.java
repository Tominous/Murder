package org.inventivetalent.murder.arena;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.inventivetalent.murder.Murder;
import org.inventivetalent.murder.arena.spawn.SpawnPoint;
import org.inventivetalent.murder.arena.spawn.SpawnType;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Arena {

	public static int ID_COUNTER = 1;

	public final int    id;
	public final String world;
	public final Set<SpawnPoint> spawnPoints = new HashSet<>();
	public Vector  minCorner;
	public Vector  maxCorner;
	public String  name;
	public int     minPlayers;
	public int     maxPlayers;
	public boolean disabled;

	public Arena(@Nonnull String world, @Nonnull String name) {
		this.id = ID_COUNTER++;
		this.world = world;
		this.name = name;
	}

	public Arena(@Nonnegative int id, @Nonnull String world, @Nonnull String name) {
		this.id = id;
		this.world = world;
		this.name = name;
	}

	public Arena(@Nonnull World world, @Nonnull String name) {
		this(world.getName(), name);
	}

	public Arena(@Nonnull JsonObject jsonObject) {
		this.id = jsonObject.get("id").getAsInt();
		if (id > ID_COUNTER) { ID_COUNTER = id; }

		this.world = jsonObject.get("world").getAsString();
		this.name = jsonObject.get("name").getAsString();
		for (Iterator<JsonElement> iterator = jsonObject.get("spawns").getAsJsonArray().iterator(); iterator.hasNext(); ) {
			spawnPoints.add(new SpawnPoint(iterator.next().getAsJsonObject()));
		}

		JsonObject boundsObject = jsonObject.getAsJsonObject("bounds");
		this.minCorner = jsonToVector(boundsObject.getAsJsonObject("min"));
		this.maxCorner = jsonToVector(boundsObject.getAsJsonObject("max"));

		JsonObject playerObject = jsonObject.getAsJsonObject("players");
		this.minPlayers = playerObject.get("min").getAsInt();
		this.maxPlayers = playerObject.get("max").getAsInt();

		this.disabled = jsonObject.has("disabled") && jsonObject.get("disabled").getAsBoolean();
	}

	public World getWorld() {
		return Bukkit.getWorld(this.world);
	}

	public Set<SpawnPoint> getSpawnPoints(SpawnType type) {
		Set<SpawnPoint> points = new HashSet<>();
		for (SpawnPoint spawnPoint : spawnPoints) {
			if (spawnPoint.type == type) { points.add(spawnPoint); }
		}
		return points;
	}

	public SpawnPoint getFirstSpawnPoint(SpawnType type) {
		for (SpawnPoint spawnPoint : spawnPoints) {
			if (spawnPoint.type == type) {return spawnPoint;}
		}
		return null;
	}

	public boolean contains(Location location) {
		return (location != null && location.getWorld() != null) && world.equals(location.getWorld().getName()) && contains(location.toVector());
	}

	public boolean contains(Vector point) {
		return Murder.instance.contains(minCorner, maxCorner, point);
	}

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("id", id);
		jsonObject.addProperty("world", world);
		jsonObject.addProperty("name", name);

		JsonArray spawnArray = new JsonArray();
		for (SpawnPoint spawnPoint : spawnPoints) {
			spawnArray.add(spawnPoint.toJson());
		}
		jsonObject.add("spawns", spawnArray);

		JsonObject boundsObject = new JsonObject();
		boundsObject.add("min", vectorToJson(minCorner));
		boundsObject.add("max", vectorToJson(maxCorner));
		jsonObject.add("bounds", boundsObject);

		JsonObject playerObject = new JsonObject();
		playerObject.addProperty("min", minPlayers);
		playerObject.addProperty("max", maxPlayers);
		jsonObject.add("players", playerObject);

		jsonObject.addProperty("disabled", disabled);

		return jsonObject;
	}

	JsonObject vectorToJson(Vector vector) {
		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("x", vector.getX());
		jsonObject.addProperty("y", vector.getY());
		jsonObject.addProperty("z", vector.getZ());

		return jsonObject;
	}

	Vector jsonToVector(JsonObject jsonObject) {
		return new Vector(jsonObject.get("x").getAsDouble(), jsonObject.get("y").getAsDouble(), jsonObject.get("z").getAsDouble());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		Arena arena = (Arena) o;

		if (id != arena.id) { return false; }
		return world != null ? world.equals(arena.world) : arena.world == null;

	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (world != null ? world.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Arena{" +
				"id=" + id +
				", world='" + world + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}

