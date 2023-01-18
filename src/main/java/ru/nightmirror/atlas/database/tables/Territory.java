package ru.nightmirror.atlas.database.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import ru.nightmirror.atlas.misc.Logging;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DatabaseTable(tableName = "atlas_territories")
public class Territory {

    @DatabaseField(id = true, columnName = "id")
    private String UUID;

    @DatabaseField(columnName = "points")
    private String serializedPoints;

    @DatabaseField(columnName = "owner_uuid")
    private String ownerUUID;

    @DatabaseField
    private String name;

    @DatabaseField
    private String description;

    @DatabaseField(columnName = "created_at")
    private Long createdAt;

    @DatabaseField(columnName = "updated_at")
    private Long updatedAt;

    public boolean isReadyToCreate() {
        return ownerUUID != null && name != null && description != null && serializedPoints != null;
    }

    @Nullable
    public LinkedHashSet<Location> getPoints() {
        try {
            LinkedHashSet<Location> points = new LinkedHashSet<>();
            Arrays.stream(serializedPoints.split("|")).forEach(pointStr -> {
                Location point = new Location(
                        Bukkit.getWorld(pointStr.split("~")[0]),
                        Integer.parseInt(pointStr.split("~")[1]),
                        Integer.parseInt(pointStr.split("~")[2]),
                        Integer.parseInt(pointStr.split("~")[3])
                );
                points.add(point);
            });
            return points;
        } catch (Exception exception) {
            Logging.error(String.format("Can't deserialize location for territory with uuid '%s' cause: %s", getUUID(), exception.getMessage()));
            if (Logging.isDebugEnabled()) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public void setPoints(LinkedHashSet<Location> points) {
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < points.size(); i++) {
            Location point = (Location) points.toArray()[i];
            String pointStr = String.format(
                    "%s~%d~%d~%d",
                    point.getWorld().getName(),
                    point.getBlockX(),
                    point.getBlockY(),
                    point.getBlockZ()
            );

            if (i != points.size()-1) {
                value.append(pointStr).append("|");
            } else {
                value.append(pointStr);
            }
        }

        serializedPoints = value.toString();
    }
}
