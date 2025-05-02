package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.Channel.PublicChannelCreateRequestDTO;
import com.sprint.mission.discodeit.dto.Message.MessageCreateRequestDTO;
import com.sprint.mission.discodeit.dto.User.UserCreateRequestDTO;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Optional;

@SpringBootApplication
public class DiscodeitApplication {

	static User setupUser(UserService userService) {
		User user = userService.create(new UserCreateRequestDTO(
			"happy","happy@codeit.com","happy1234"
		), Optional.empty());
		return user;
	}

	static Channel setupPublicChannel(ChannelService channelService) {
		Channel publicChannel = channelService.create(new PublicChannelCreateRequestDTO(
				"잡담", "잡담채널입니다."
		));
		return publicChannel;
	}

	static void messageCreateTest(MessageService messageService, Channel channel, User author) {
		Message message = messageService.create(new MessageCreateRequestDTO(
				"안녕하세요", author.getId(), channel.getId()
		), new ArrayList<>());
		System.out.println("메시지 생성: " + message);
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		// 서비스 초기화
		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);

		// 셋업
		User user = setupUser(userService);
		Channel channel = setupPublicChannel(channelService);
		// 테스트
		messageCreateTest(messageService, channel, user);
	}
}
