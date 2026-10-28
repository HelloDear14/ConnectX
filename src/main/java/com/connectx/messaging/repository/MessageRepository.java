package com.connectx.messaging.repository;

import com.connectx.messaging.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChannelIdAndDeletedFalseOrderByCreatedAtDesc(Long channelId, Pageable pageable);
}
