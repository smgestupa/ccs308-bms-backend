package com.bms.backend.models.book

import com.fasterxml.jackson.annotation.JsonManagedReference
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table
class Book constructor(
        cover: ByteArray?,
        title: String,
        author: String?,
        description: String?
) {

    @Id
    @Column(name="book_id")
    @GeneratedValue(strategy=GenerationType.AUTO)
    val bookID: Int = 0;

    @Column
    val cover: ByteArray?;

    @Column(
        length=128,
        nullable=false
    )
    val title: String;

    @Column(length=64)
    val author: String?;

    @Column(length=1024)
    val description: String?;

    @Column(name="created_at")
    @CreationTimestamp
    lateinit var createdAt: LocalDateTime;

    @Column(name="updated_at")
    @CreationTimestamp
    lateinit var updatedAt: LocalDateTime;

    @OneToOne(
            cascade = [CascadeType.ALL],
            fetch = FetchType.LAZY
    )
    @JoinColumn(name="book_id")
    lateinit var bookMetadata: BookMetadata;

    init {
        this.cover = cover;
        this.title = title;
        this.author = author;
        this.description = description;
    }
}
