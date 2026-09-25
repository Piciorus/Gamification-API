```
package de.consorsbank.core.trauthsc.tam.template.entity.enums;

public enum TemplateStatus {
    DRAFT,
    ACTIVE,
    DEPRECATED,
    ARCHIVED
}

```


```
package de.consorsbank.core.trauthsc.tam.template.entity.enums;

public enum TemplateLanguage {
    DE,
    EN,
    FR,
    NL
}

```


```
package de.consorsbank.core.trauthsc.tam.template.entity.enums;

public enum LifecycleStatus {
    DRAFT,
    ACTIVE,
    DEPRECATED,
    ARCHIVED
}

```


```
package de.consorsbank.core.trauthsc.tam.template.entity.enums;

public enum ValidationStatus {
    PENDING,
    PASSED,
    FAILED
}

```



```
package de.consorsbank.core.trauthsc.tam.template.entity.enums;

public enum ValidationType {
    SYNTAX,
    METADATA,
    PLACEHOLDER,
    COMPATIBILITY,
    BUSINESS
}


```


```
package de.consorsbank.core.trauthsc.tam.template.entity.enums;

public enum OwnerSystemStatus {
    ACTIVE,
    INACTIVE
}


```


```

package de.consorsbank.core.trauthsc.tam.template.entity.enums;

public enum PromotionStatus {
    PENDING,
    SUCCESS,
    FAILED
}

```

```
package de.consorsbank.core.trauthsc.tam.template.entity;

import de.consorsbank.core.trauthsc.tam.template.entity.base.BaseAuditEntity;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.OwnerSystemStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "OWNER_SYSTEM", schema = "TAM")
@Getter
@Setter
@NoArgsConstructor
public class OwnerSystemEntity extends BaseAuditEntity {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    @Column(name = "SYSTEM_NAME", length = 100, nullable = false, unique = true)
    private String systemName;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private OwnerSystemStatus status;
}


```


```

package de.consorsbank.core.trauthsc.tam.template.entity;

import de.consorsbank.core.trauthsc.tam.template.entity.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "CATEGORY", schema = "TAM")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity extends BaseAuditEntity {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    @Column(name = "NAME", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;
}


```



```
package de.consorsbank.core.trauthsc.tam.template.entity;

import de.consorsbank.core.trauthsc.tam.template.entity.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "TAG", schema = "TAM")
@Getter
@Setter
@NoArgsConstructor
public class TagEntity extends BaseAuditEntity {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    @Column(name = "VALUE", length = 100, nullable = false, unique = true)
    private String value;
}

```


```
package de.consorsbank.core.trauthsc.tam.template.entity;

import de.consorsbank.core.trauthsc.tam.template.entity.base.BaseAuditEntity;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.TemplateLanguage;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.TemplateStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "TEMPLATE", schema = "TAM")
@Getter
@Setter
@NoArgsConstructor
public class TemplateEntity extends BaseAuditEntity {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_SYSTEM_ID", nullable = false)
    private OwnerSystemEntity ownerSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private CategoryEntity category;

    // Stored as plain column to avoid circular FK issue with TemplateVersionEntity.
    // Use TemplateVersionRepository.findById(activeVersionId) to resolve when needed.
    @Column(name = "ACTIVE_VERSION_ID", columnDefinition = "RAW(16)")
    private UUID activeVersionId;

    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "LANGUAGE", length = 10, nullable = false)
    private TemplateLanguage language;

    @Column(name = "NAME", length = 255, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private TemplateStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "TEMPLATE_TAG",
            schema = "TAM",
            joinColumns = @JoinColumn(name = "TEMPLATE_ID"),
            inverseJoinColumns = @JoinColumn(name = "TAG_ID")
    )
    private Set<TagEntity> tags = new HashSet<>();
}


```



```

package de.consorsbank.core.trauthsc.tam.template.entity;

import de.consorsbank.core.trauthsc.tam.template.entity.enums.LifecycleStatus;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.ValidationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

// Immutable by design — no BaseAuditEntity, no updated_*/deleted_*/version columns.
// Every change creates a new version row. Never update existing rows.
@Entity
@Table(name = "TEMPLATE_VERSION", schema = "TAM")
@Getter
@Setter
@NoArgsConstructor
public class TemplateVersionEntity {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private TemplateEntity template;

    @Column(name = "VERSION_NUMBER", nullable = false)
    private Long versionNumber;

    @Column(name = "TEMPLATE_CONTENT", nullable = false, columnDefinition = "CLOB")
    private String templateContent;

    @Column(name = "CHECKSUM", length = 64, nullable = false)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "LIFECYCLE_STATUS", length = 20, nullable = false)
    private LifecycleStatus lifecycleStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "VALIDATION_STATUS", length = 20, nullable = false)
    private ValidationStatus validationStatus;

    @Column(
            name = "CREATED_AT",
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP",
            updatable = false,
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "CREATED_BY", length = 25, nullable = false, updatable = false)
    private String createdBy;
}

```


```

package de.consorsbank.core.trauthsc.tam.template.entity;

import de.consorsbank.core.trauthsc.tam.template.entity.enums.ValidationStatus;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.ValidationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

// Immutable, append-only — validation history must never be mutated.
@Entity
@Table(name = "VALIDATION_RESULT", schema = "TAM")
@Getter
@Setter
@NoArgsConstructor
public class ValidationResultEntity {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VERSION_ID", nullable = false)
    private TemplateVersionEntity version;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", length = 50, nullable = false)
    private ValidationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private ValidationStatus status;

    @Column(name = "ERROR_DETAILS", columnDefinition = "CLOB")
    private String errorDetails;

    @Column(
            name = "EXECUTED_AT",
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP",
            updatable = false,
            nullable = false
    )
    private OffsetDateTime executedAt;

    @Column(name = "EXECUTED_BY", length = 25, nullable = false, updatable = false)
    private String executedBy;
}

```

```
package de.consorsbank.core.trauthsc.tam.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

// BaFin-mandatory — immutable, append-only, 10-year retention.
// No FK constraints to template/version: audit records must survive deletions.
// No BaseAuditEntity: audit log is itself the audit trail.
@Entity
@Table(name = "AUDIT_LOG", schema = "TAM")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogEntity {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    // No @ManyToOne — intentional. Audit records survive template deletion.
    @Column(name = "TEMPLATE_ID", columnDefinition = "RAW(16)", nullable = false, updatable = false)
    private UUID templateId;

    @Column(name = "VERSION_ID", columnDefinition = "RAW(16)", updatable = false)
    private UUID versionId;

    @Column(name = "OPERATION", length = 50, nullable = false, updatable = false)
    private String operation;

    @Column(name = "ACTOR", length = 100, nullable = false, updatable = false)
    private String actor;

    @Column(
            name = "TIMESTAMP",
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP",
            updatable = false,
            nullable = false
    )
    private OffsetDateTime timestamp;

    // Stored as JSON string — Oracle has no native JSONB, use CLOB
    @Column(name = "OLD_VALUE", columnDefinition = "CLOB", updatable = false)
    private String oldValue;

    @Column(name = "NEW_VALUE", columnDefinition = "CLOB", updatable = false)
    private String newValue;
}

```


```
package de.consorsbank.core.trauthsc.tam.template.entity;

import de.consorsbank.core.trauthsc.tam.template.entity.base.BaseAuditEntity;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.Environment;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "PROMOTION_HISTORY", schema = "TAM")
@Getter
@Setter
@NoArgsConstructor
public class PromotionHistoryEntity extends BaseAuditEntity {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private TemplateEntity template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VERSION_ID", nullable = false)
    private TemplateVersionEntity version;

    @Enumerated(EnumType.STRING)
    @Column(name = "SOURCE_ENVIRONMENT", length = 50, nullable = false)
    private Environment sourceEnvironment;

    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_ENVIRONMENT", length = 50, nullable = false)
    private Environment targetEnvironment;

    @Column(name = "PROMOTED_BY", length = 100, nullable = false)
    private String promotedBy;

    @Column(
            name = "PROMOTED_AT",
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP",
            nullable = false
    )
    private OffsetDateTime promotedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private PromotionStatus status;
}

```

```

package de.consorsbank.core.trauthsc.tam.template.repository;

import de.consorsbank.core.trauthsc.tam.template.entity.OwnerSystemEntity;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.OwnerSystemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwnerSystemRepository extends JpaRepository<OwnerSystemEntity, UUID> {

    Optional<OwnerSystemEntity> findBySystemNameAndIsDeletedFalse(String systemName);

    List<OwnerSystemEntity> findAllByStatusAndIsDeletedFalse(OwnerSystemStatus status);
}

```


```
package de.consorsbank.core.trauthsc.tam.template.repository;

import de.consorsbank.core.trauthsc.tam.template.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findByNameAndIsDeletedFalse(String name);

    List<CategoryEntity> findAllByIsDeletedFalse();
}

```


```
package de.consorsbank.core.trauthsc.tam.template.repository;

import de.consorsbank.core.trauthsc.tam.template.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {

    Optional<TagEntity> findByValueAndIsDeletedFalse(String value);

    List<TagEntity> findAllByIsDeletedFalse();
}

```


```
package de.consorsbank.core.trauthsc.tam.template.repository;

import de.consorsbank.core.trauthsc.tam.template.entity.TemplateEntity;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, UUID> {

    Optional<TemplateEntity> findByIdAndIsDeletedFalse(UUID id);

    // Business key lookup — code + language + owner
    Optional<TemplateEntity> findByCodeAndLanguageAndOwnerSystemIdAndIsDeletedFalse(
            String code,
            String language,
            UUID ownerSystemId
    );

    List<TemplateEntity> findAllByStatusAndIsDeletedFalse(TemplateStatus status);

    List<TemplateEntity> findAllByOwnerSystemIdAndIsDeletedFalse(UUID ownerSystemId);

    List<TemplateEntity> findAllByCategoryIdAndIsDeletedFalse(UUID categoryId);

    boolean existsByCodeAndLanguageAndOwnerSystemIdAndIsDeletedFalse(
            String code,
            String language,
            UUID ownerSystemId
    );
}

```


```

package de.consorsbank.core.trauthsc.tam.template.repository;

import de.consorsbank.core.trauthsc.tam.template.entity.TemplateVersionEntity;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.LifecycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateVersionRepository extends JpaRepository<TemplateVersionEntity, UUID> {

    List<TemplateVersionEntity> findAllByTemplateIdOrderByVersionNumberDesc(UUID templateId);

    Optional<TemplateVersionEntity> findByTemplateIdAndLifecycleStatus(UUID templateId, LifecycleStatus status);

    // Latest version by version_number — used when creating a new version
    @Query("SELECT MAX(v.versionNumber) FROM TemplateVersionEntity v WHERE v.template.id = :templateId")
    Optional<Long> findMaxVersionNumberByTemplateId(@Param("templateId") UUID templateId);

    List<TemplateVersionEntity> findAllByTemplateIdAndLifecycleStatus(UUID templateId, LifecycleStatus status);
}
```


```
package de.consorsbank.core.trauthsc.tam.template.repository;

import de.consorsbank.core.trauthsc.tam.template.entity.ValidationResultEntity;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.ValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ValidationResultRepository extends JpaRepository<ValidationResultEntity, UUID> {

    List<ValidationResultEntity> findAllByVersionId(UUID versionId);

    List<ValidationResultEntity> findAllByVersionIdAndStatus(UUID versionId, ValidationStatus status);
}

```


```
package de.consorsbank.core.trauthsc.tam.template.repository;

import de.consorsbank.core.trauthsc.tam.template.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    List<AuditLogEntity> findAllByTemplateIdOrderByTimestampDesc(UUID templateId);

    List<AuditLogEntity> findAllByTemplateIdAndVersionIdOrderByTimestampDesc(UUID templateId, UUID versionId);

    List<AuditLogEntity> findAllByActorOrderByTimestampDesc(String actor);
}

```


```
package de.consorsbank.core.trauthsc.tam.template.repository;

import de.consorsbank.core.trauthsc.tam.template.entity.PromotionHistoryEntity;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.Environment;
import de.consorsbank.core.trauthsc.tam.template.entity.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromotionHistoryRepository extends JpaRepository<PromotionHistoryEntity, UUID> {

    List<PromotionHistoryEntity> findAllByTemplateIdAndIsDeletedFalse(UUID templateId);

    List<PromotionHistoryEntity> findAllByTemplateIdAndVersionIdAndIsDeletedFalse(UUID templateId, UUID versionId);

    List<PromotionHistoryEntity> findAllByTargetEnvironmentAndStatusAndIsDeletedFalse(
            Environment targetEnvironment,
            PromotionStatus status
    );
}

```
