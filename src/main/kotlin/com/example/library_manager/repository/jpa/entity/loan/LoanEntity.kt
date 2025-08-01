package com.example.library_manager.repository.jpa.entity.loan

import com.example.library_manager.repository.jpa.entity.library_item.LibraryItemEntity
import com.example.library_manager.repository.jpa.entity.reader.ReaderEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "loan")
@NamedEntityGraph(
    name = "LoanEntity.withLibraryItem",
    attributeNodes = [NamedAttributeNode("libraryItemEntity")]
)
data class LoanEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    val readerEntity: ReaderEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_item_id", nullable = true)
    val libraryItemEntity: LibraryItemEntity?,

    @Column(name = "loan_date", nullable = false)
    val loanDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "return_date")
    val returnDate: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}