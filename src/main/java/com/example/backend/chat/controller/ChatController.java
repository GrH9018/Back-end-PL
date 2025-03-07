package com.example.backend.chat.controller;

import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.backend.chat.dto.ChatRoomDto;
import com.example.backend.chat.dto.MessageDto;
import com.example.backend.chat.entity.ChatMessage;
import com.example.backend.chat.entity.ChatRoom;
import com.example.backend.chat.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

	private final SimpMessagingTemplate simpMessagingTemplate;
	private final ChatService chatService;

	@MessageMapping("/user/{user-id}")
	public void sendMessage(@DestinationVariable(value = "user-id") Long receiverId, MessageDto message) {
		log.info("chat {} send by {} to room number{}", message.getMessage(), message.getSenderId(), receiverId);
		chatService.saveMessages(receiverId,message);
		simpMessagingTemplate.convertAndSend("/sub/user/"+receiverId, message);
	}

	@ResponseBody
	@GetMapping("/chat/{roomName}/chat-list")
	public List<ChatMessage> getAllMessages(@PathVariable String roomName){
		return chatService.getAllMessages(roomName);
	}

	@ResponseBody
	@GetMapping("/chat/{user-id}/room-list") //이러면 다른사람 채팅방 목록도 볼 수 있음 토큰에서 가져와야할듯
	public List<ChatRoomDto> getAllRooms(@PathVariable(value = "user-id") Long userId){
		return chatService.getAllRooms(userId);
	}
}
