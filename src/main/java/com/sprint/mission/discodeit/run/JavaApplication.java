package com.sprint.mission.discodeit.run;

import com.sprint.mission.discodeit.menu.ChannelMenu;
import com.sprint.mission.discodeit.menu.ChatMenu;
import com.sprint.mission.discodeit.menu.UserMenu;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.ArrayList;
import java.util.Scanner;


public class JavaApplication {

    public static void main(String[] args) {

        UserService userService = new JCFUserService(new ArrayList<>());
        MessageService messageService = new JCFMessageService(new ArrayList<>());
        ChannelService channelService = new JCFChannelService(new ArrayList<>());

        // CASE 1. Scanner로 입력받아 각 기능 구현
        boolean running = true;

        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                int choice = 0;
                UserMenu userMenu = new UserMenu(userService);
                ChannelMenu channelMenu = new ChannelMenu(channelService);
                ChatMenu chatMenu = new ChatMenu(userService, channelService, messageService);

                System.out.println("========= 디스코드잇 =========");
                System.out.println("1. 유저 관리");
                System.out.println("2. 채널 관리");
                System.out.println("3. 채팅 관리");
                System.out.println("0. 종료 ");

                while (true) {
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
        }
    }
}
