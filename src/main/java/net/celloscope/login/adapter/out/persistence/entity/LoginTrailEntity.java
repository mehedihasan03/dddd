package net.celloscope.login.adapter.out.persistence.entity;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("login_trail")
public class LoginTrailEntity implements Persistable<String> {

    @Id
    private String oid;
    private String userOid;
    private String type;
    private String sourceIp;

    @CreatedDate
    private Timestamp inOutTime;

    @Override
    public String getId() {
        return this.oid;
    }

    @Override
    public boolean isNew() {
        boolean isNull = Objects.isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
    }

}
