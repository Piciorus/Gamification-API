```
package de.consorsbank.core.trauthsc.tam.entity;

import de.consorsbank.core.trauthsc.tam.entity.enums.AuthorizationAttemptStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "AUTHORIZATION_ATTEMPTS", schema = "TAM")
@Getter
@Setter
@NoArgsConstructor
public class AuthorizationAttemptEntity {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    @Column(name = "EXTERNAL_ID", columnDefinition = "RAW(16)", nullable = false, unique = true)
    private UUID externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHORIZATION_ID", nullable = false)
    private AuthorizationEntity authorization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHORIZATION_METHOD", referencedColumnName = "NAME", nullable = false)
    private AuthorizationMethodEntity authorizationMethod;

    @Column(name = "AUTHORIZATION_CREDENTIAL", length = 20)
    private String authorizationCredential;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private AuthorizationAttemptStatusEnum status;

    @Column(name = "IS_DELETED", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "CREATED_BY", length = 25, nullable = false)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 25, nullable = false)
    private String updatedBy;

    @Column(name = "DELETED_BY", length = 25)
    private String deletedBy;

    @Column(
            name = "CREATED_AT",
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP",
            updatable = false,
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "UPDATED_AT",
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @Column(
            name = "DELETED_AT",
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime deletedAt;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Long version = 1L;
}

```


```
@OneToMany(mappedBy = "service", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
private List<ServiceAuthorizationMethodEntity> authorizationMethods = new ArrayList<>();
```

```
package de.consorsbank.core.trauthsc.tam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "SERVICES_AUTHORIZATION_METHODS",
    schema = "TAM",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_tam_services_auth_methods",
        columnNames = {"AUTHORIZATION_METHOD", "OWNER", "SERVICE", "SERVICE_VERSION"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class ServiceAuthorizationMethodEntity {

    @EmbeddedId
    private ServiceAuthorizationMethodId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "OWNER", referencedColumnName = "OWNER", insertable = false, updatable = false),
            @JoinColumn(name = "SERVICE", referencedColumnName = "SERVICE", insertable = false, updatable = false),
            @JoinColumn(name = "SERVICE_VERSION", referencedColumnName = "SERVICE_VERSION", insertable = false, updatable = false)
    })
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHORIZATION_METHOD", referencedColumnName = "NAME", insertable = false, updatable = false)
    private AuthorizationMethodEntity authorizationMethod;

    @Column(name = "IS_DELETED", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "CREATED_BY", length = 25, nullable = false)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 25, nullable = false)
    private String updatedBy;

    @Column(name = "DELETED_BY", length = 25)
    private String deletedBy;

    @Column(name = "CREATED_AT", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "DELETED_AT", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime deletedAt;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Long version = 1L;
}
```

```
package de.consorsbank.core.trauthsc.tam.entity.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ServiceAuthorizationMethodId implements Serializable {

    @Column(name = "AUTHORIZATION_METHOD", length = 40, nullable = false)
    private String authorizationMethod;

    @Column(name = "OWNER", length = 30, nullable = false)
    private String owner;

    @Column(name = "SERVICE", length = 50, nullable = false)
    private String service;

    @Column(name = "SERVICE_VERSION", length = 25, nullable = false)
    private String serviceVersion;
}
```

```
databaseChangeLog:
  - changeSet:
      id: 003-create-services-authorization-methods-table
      author: vlad.pop@externe.bnpparibas.com
      changes:
        - createTable:
            tableName: services_authorization_methods
            schemaName: tam
            columns:
              - column:
                  name: owner
                  type: VARCHAR2(30)
                  constraints:
                    nullable: false
              - column:
                  name: service
                  type: VARCHAR2(50)
                  constraints:
                    nullable: false
              - column:
                  name: service_version
                  type: VARCHAR2(25)
                  constraints:
                    nullable: false
              - column:
                  name: authorization_method
                  type: VARCHAR2(40)
                  constraints:
                    nullable: false
              - column:
                  name: is_deleted
                  type: NUMBER(1)
                  defaultValue: 0
                  constraints:
                    nullable: false
              - column:
                  name: created_by
                  type: VARCHAR2(25)
                  constraints:
                    nullable: false
              - column:
                  name: updated_by
                  type: VARCHAR2(25)
                  constraints:
                    nullable: false
              - column:
                  name: deleted_by
                  type: VARCHAR2(25)
              - column:
                  name: created_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValue: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: updated_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValue: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: deleted_at
                  type: TIMESTAMP WITH TIME ZONE
              - column:
                  name: version
                  type: NUMBER(19)
                  defaultValue: 1
                  constraints:
                    nullable: false

        - addPrimaryKey:
            tableName: services_authorization_methods
            schemaName: tam
            columnNames: authorization_method, owner, service, service_version
            constraintName: pk_tam_services_authorization_methods

        - addForeignKeyConstraint:
            constraintName: fk_tam_services_auth_methods_services
            baseTableSchemaName: tam
            baseTableName: services_authorization_methods
            baseColumnNames: owner, service, service_version
            referencedTableSchemaName: tam
            referencedTableName: services
            referencedColumnNames: owner, service, service_version
            onDelete: CASCADE

        - addForeignKeyConstraint:
            constraintName: fk_tam_services_auth_methods_auth_methods
            baseTableSchemaName: tam
            baseTableName: services_authorization_methods
            baseColumnNames: authorization_method
            referencedTableSchemaName: tam
            referencedTableName: authorization_methods
            referencedColumnNames: name
```
