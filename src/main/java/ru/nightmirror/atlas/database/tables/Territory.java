package ru.nightmirror.atlas.database.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
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
}
