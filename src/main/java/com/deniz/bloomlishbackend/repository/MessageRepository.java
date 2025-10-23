package com.deniz.bloomlishbackend.repository;

import com.deniz.bloomlishbackend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MessageRepository extends JpaRepository <Message, Long> {
    //sohbet geçmişini getirme sorgusu
    @Query("SELECT m FROM Message m WHERE (m.sender.userID = :user1id AND m.receiver.userID = :user2id) OR (m.sender.userID = :user2id AND m.receiver.userID= :user1id) ORDER BY m.sentAt ASC")
        List<Message> findConversation(Long user1id,Long user2id);

}
