package com.trillion.tikitaka.attachment.infrastructure;

import com.trillion.tikitaka.attachment.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
