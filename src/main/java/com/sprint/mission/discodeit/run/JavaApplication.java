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
            System.out.println("* 프로그램 종료 시, 하드코딩 데이터로 구현한 기능 실행 결과 확인 가능 ");

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

        // 데이터 초기화
        userList.clear();
        channelList.clear();
        messageList.clear();

        // CASE 2. 각 기능 구현 결과 하드코딩
        System.out.println("---------- 하드 코딩 결과 출력 ----------");
        // 1. 등록
        // 1-1. 유저 등록
        System.out.println("-------- 유저 등록 -----------");
        User user1 = userService.createUser("민수","a123");
        User user2 = userService.createUser("철수","b123");
        User user3 = userService.createUser("영희","c123");

        // 1-2. 채널 등록
        System.out.println("-------- 채널 등록 -----------");
        Channel ch1 = channelService.createChannel("채널1");
        Channel ch2 = channelService.createChannel("채널2");
        Channel ch3 = channelService.createChannel("채널3");

        // 유저 방 입장 (동일 유저가 여러 개의 방에 입장 가능)
        System.out.println("-------- 유저 방 입장  -----------");
        channelService.enterChannel(user1, ch1);    // 민수가 채널1 입장
        channelService.enterChannel(user2, ch2);    // 철수가 채널2 입장
        channelService.enterChannel(user1, ch2);    // 민수가 채널2 입장
        channelService.enterChannel(user3, ch3);    // 영희가 채널3 입장

        // 1-3. 메시지 등록
        Message msg1 = messageService.createMessage(ch1, user1, "안녕하세요 코드잇!");  // 채널1에 민수가 메시지 입력
        Message msg2 = messageService.createMessage(ch2, user2, "여기는 2번방, 입장!");    // 채널2에 철수가 메시지 입력
        Message msg3 = messageService.createMessage(ch2, user2, "ㅎㅎ 2번방에 철수 입장!");  // 채널2에 철수가 메시지 입력
        Message msg4 = messageService.createMessage(ch1, user2, "채널1에 철수가 메시지를 입력");    // 채널1에 철수가 메시지 입력
        Message msg5 = messageService.createMessage(ch3, user3, "마이네임영희");  // 채널3에 영희가 메시지 입력

        // 2. 다건 조회
        // 2-1. 유저 다건 조회
        System.out.println("-------- 유저 다건 조회 -----------");
        System.out.println(userService.getUsers());

        // 2-2. 채널 다건 조회
        System.out.println("-------- 채널 다건 조회 -----------");
        System.out.println(channelService.getChannels());

        // 2-3. 메시지 다건 조회
        System.out.println("-------- 메시지 다건 조회 -----------");
        System.out.println(messageService.getMessages());

        // 3. 단건 조회
        // 3-1. 유저 단건 조회
        System.out.println("-------- 유저 단건 조회 -----------");
        System.out.println(userService.getUser(user1.getId())); // 민수 조회

        // 3-2. 채널 단건 조회
        System.out.println("-------- 채널 단건 조회 -----------");
        System.out.println(channelService.getChannel(ch1.getId())); // 채널1 조회

        // 3-3. 메시지 단건 조회
        System.out.println("-------- 메시지 단건 조회 -----------");
        System.out.println(messageService.getMessage(msg1.getId()));    // 메시지1 조회

        // 4. 수정
        // 4-1. 유저 수정
        System.out.println("-------- 유저 수정  -----------");
        userService.updateUser(user1, "민수2");    // 민수 -> 민수2 수정
        // 4-2. 채널 수정
        System.out.println("-------- 채널 수정  -----------");
        channelService.updateChannel(ch1,"1번방"); // 채널1 -> 1번방 수정
        // 4-3. 메시지 수정
        System.out.println("-------- 메시지 수정  -----------");
        messageService.updateMessage(msg1, "반갑습니다 코드잇!");   // 메시지1 수정

        // 5. 수정된 데이터 조회
        // 5-1. 유저 수정 데이터 조회
        System.out.println("-------- 유저 수정건 조회 -----------");
        System.out.println(userService.getUser(user1.getId()));

        // 5-2. 채널 수정 데이터 조회
        System.out.println("-------- 채널 수정건 조회 -----------");
        System.out.println(channelService.getChannel(ch1.getId()));

        // 5-3. 메시지 수정 데이터 조회
        System.out.println("-------- 메시지 수정건 조회 -----------");
        System.out.println(messageService.getMessage(msg1.getId()));

        // 6. 삭제
        // 6-1. 유저 삭제
        System.out.println("-------- 유저 삭제  -----------");
        userService.deleteUser(user2.getId());  // 철수 삭제
        // 6-2. 채널 삭제
        System.out.println("-------- 채널 삭제  -----------");
        channelService.deleteChannel(ch1.getId());  // 1번방 삭제
        // 6-3. 메시지 삭제
        System.out.println("-------- 메시지 삭제  -----------");
        messageService.deleteMessage(msg1.getId()); // 메시지1 삭제
        messageService.deleteMessage(msg2.getId()); // 메시지2 삭제

        // 7. 전체 조회를 통해 삭제되었는지 확인
        // 7-1. 유저 삭제 확인
        System.out.println("-------- 유저 삭제 조회 (모든 유저) -----------");
        for (User user : userList) {
            System.out.println(userService.getUser(user.getId()));
        }

        // 7-2. 채널 삭제 확인
        System.out.println("-------- 채널 삭제 조회 (모든 채널)-----------");
        for (Channel channel : channelList) {
            System.out.println(channelService.getChannel(channel.getId()));
        }

        // 7-3. 메시지 삭제 확인
        System.out.println("-------- 메시지 삭제 조회 (모든 메시지)-----------");
        for (Message msg : messageList) {
            System.out.println(messageService.getMessage(msg.getId()));
        }

        // +) 유저 방 퇴장
        System.out.println("-------- 유저 방 퇴장  -----------");
        channelService.leaveChannel(user1, ch2);

        // 전체 조회
        System.out.println("----------- 전체 조회 -----------");
        System.out.println(userService.getUsers());
        System.out.println(channelService.getChannels());
        System.out.println(messageService.getMessages());
    }
}
