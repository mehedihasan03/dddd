package net.celloscope.login.adapter.out.persistence.entity;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mfi")
public class MfiEntity {

    @Id
    private String oid;
    private String mfiName;
    private String licenseNumber;
    private Timestamp createdOn;
    private String createdBy;
    private String status;

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
    }
}
