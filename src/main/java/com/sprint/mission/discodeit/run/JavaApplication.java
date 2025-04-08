package com.sprint.mission.discodeit.run;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.menu.channelMenu;
import com.sprint.mission.discodeit.menu.chatMenu;
import com.sprint.mission.discodeit.menu.userMenu;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class JavaApplication {

    public static void main(String[] args) {
        List<User> userList = new ArrayList<>();
        List<Channel> channelList = new ArrayList<>();
        List<Message> messageList = new ArrayList<>();

        JCFUserService userService = new JCFUserService(userList);
        JCFMessageService messageService = new JCFMessageService(messageList);
        JCFChannelService channelService = new JCFChannelService(channelList);

        // CASE 1. Scanner로 입력받아 각 기능 구현
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {

            int choice = 0;
            userMenu userMenu = new userMenu(userService);
            channelMenu channelMenu = new channelMenu(channelService);
            chatMenu chatMenu = new chatMenu(userService, channelService, messageService);

            System.out.println("========= 디스코드잇 =========");
            System.out.println("1. 유저 관리");
            System.out.println("2. 채널 관리");
            System.out.println("3. 채팅 관리");
            System.out.println("0. 종료 ");

            while(true) {
                System.out.print("> ");
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();
                    break;
                } else {
                    System.out.println("숫자를 입력해주세요!");
                    scanner.nextLine();
                }
            }

            switch (choice) {
                case 1:
                    userMenu.run(scanner);
                    break;
                case 2:
                    channelMenu.run(scanner);
                    break;
                case 3:
                    chatMenu.run(scanner);
                    break;
                case 0:
                    running = false;
                    System.out.println("프로그램을 종료합니다.");
                    break;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }

        scanner.close();

    }
}
