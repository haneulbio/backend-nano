package com.haneul.nanobackend

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant


@Entity
@Table(
    name = "quotes",
)
class QuoteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false)
    var content: String= "",
    @Column(nullable = false)
    var author: String = "",
    @CreationTimestamp
    var created: Instant = Instant.now(),
)