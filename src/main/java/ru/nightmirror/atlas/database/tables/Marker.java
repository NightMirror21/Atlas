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

@Data
@AllArgsConstructor
@NoArgsConstructor
@DatabaseTable(tableName = "atlas_markers")
public class Marker {

    @DatabaseField(id = true, columnName = "id")
    private String UUID;

    @DatabaseField(columnName = "owner_uuid")
    private String ownerUUID;

    @DatabaseField(columnName = "point")
    private String serializedPoint;

    @DatabaseField
    private String name;

    @DatabaseField
    private String description;

    @DatabaseField(columnName = "created_at")
    private Long createdAt;

    @DatabaseField(columnName = "updated_at")
    private Long updatedAt;

    @DatabaseField
    private String type;

    public boolean isReadyToCreate() {
        return ownerUUID != null && name != null && description != null && serializedPoint != null;
    }

    @Nullable
    public Location getPoint() {
        try {
            String pointStr = serializedPoint;
            Location point = new Location(
                    Bukkit.getWorld(pointStr.split("~")[0]),
                    Integer.parseInt(pointStr.split("~")[1]),
                    Integer.parseInt(pointStr.split("~")[2]),
                    Integer.parseInt(pointStr.split("~")[3])
            );
            return point;
        } catch (Exception exception) {
            Logging.error(String.format("Can't deserialize location for marker with uuid '%s' cause: %s", getUUID(), exception.getMessage()));
            if (Logging.isDebugEnabled()) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public void setPoint(Location point) {
        serializedPoint = String.format(
                "%s~%d~%d~%d",
                point.getWorld().getName(),
                point.getBlockX(),
                point.getBlockY(),
                point.getBlockZ()
        );
    }
}
