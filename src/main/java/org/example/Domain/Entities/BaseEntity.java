package org.example.Domain.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public class BaseEntity {
    @Id
    @GeneratedValue(generator = "SEQ_USER")
    @GenericGenerator(name = "SEQ_USER", strategy = "uuid2")
    public UUID id;
    @Column(name = "CreationDate", nullable = true, length = 50)
    public Date creationDate;
    @Column(name = "UpdateDate", nullable = true, length = 50)
    public Date updateDate;
}