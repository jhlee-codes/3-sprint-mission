package com.sprint.mission.discodeit.menu;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.Scanner;

public class chatMenu {
    private final JCFUserService userService;
    private final JCFChannelService channelService;
    private final JCFMessageService messageService;

    public chatMenu(JCFUserService userService, JCFChannelService channelService, JCFMessageService messageService) {
        this.userService = userService;
        this.channelService = channelService;
        this.messageService = messageService;
    }

    public void run(Scanner scanner) {
        boolean back = false;
        Channel joinCh = null;
        User joinUser = null;

        while (!back) {
            if (joinCh == null || joinUser == null) {
                System.out.println("\n[채널 입장이 필요합니다]");
                System.out.print("사용자 ID 입력: ");
                String userId = scanner.nextLine();
                joinUser = userService.searchUserByUserId(userId);

                System.out.print("입장할 채널 이름 입력: ");
                String chNm = scanner.nextLine();
                joinCh = channelService.searchChannelByChannelName(chNm);

                if (joinUser != null && joinCh != null) {
                    channelService.enterChannel(joinUser, joinCh);
                } else if (joinUser == null) {
                    System.out.println("존재하지 않는 유저입니다. 유저 생성을 진행해주세요.(유저 관리 > 유저 생성)");
                    back = true;
                } else if (joinCh == null) {
                    System.out.println("존재하지 않는 채널입니다. 채널 생성을 진행해주세요.(채널 관리 > 채널 생성)");
                    back = true;
                }

                continue;
            }

            while (!back) {
                int choice = 0;
                Message targetMsg;
                String targetContent;

                System.out.println("\n[채팅 진행]");
                System.out.println("1. 메시지 생성");
                System.out.println("2. 메시지 전체 조회");
                System.out.println("3. 메시지 단일 조회 (검색)");
                System.out.println("4. 메시지 수정");
                System.out.println("5. 메시지 삭제");
                System.out.println("6. 채널 퇴장");
                System.out.println("0. 이전 메뉴");

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
                        System.out.print("메시지 입력: ");
                        targetContent = scanner.nextLine();
                        messageService.createMessage(joinCh, joinUser, targetContent);
                        break;
                    case 2:
                        System.out.println("전체 메시지 조회: \n" + messageService.getMessages());
                        break;
                    case 3:
                        System.out.print("조회할 메시지 입력: ");
                        targetContent = scanner.next();
                        targetMsg = messageService.searchContentByMessage(targetContent);
                        scanner.nextLine();
                        if (targetMsg == null) {
                            System.out.println("존재하지 않는 메시지입니다.");
                        } else {
                            System.out.println(targetMsg);
                        }
                        break;
                    case 4:
                        System.out.print("수정할 메시지 내용 입력: ");
                        targetContent = scanner.next();
                        scanner.nextLine();
                        System.out.print("수정할 메시지 내용 입력: ");
                        String newContent = scanner.nextLine();
                        targetMsg = messageService.searchContentByMessage(targetContent);
                        messageService.updateMessage(targetMsg, newContent);
                        break;
                    case 5:
                        System.out.print("삭제할 메시지 입력: ");
                        targetContent = scanner.next();
                        scanner.nextLine();
                        targetMsg = messageService.searchContentByMessage(targetContent);
                        if (targetMsg == null) {
                            System.out.println("존재하지 않는 메시지입니다.");
                        } else {
                            messageService.deleteMessage(targetMsg.getId());
                        }
                        break;
                    case 6:
                        channelService.leaveChannel(joinUser, joinCh);
                        back = true;
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("잘못된 선택입니다.");
                }
            }
        }
    }
}
