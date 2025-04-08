package com.sprint.mission.discodeit.menu;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;

import java.util.Scanner;

public class channelMenu {

    private final JCFChannelService channelService;

    public channelMenu(JCFChannelService channelService) {
        this.channelService = channelService;
    }

    public void run(Scanner scanner) {
        boolean back = false;

        while (!back) {
            System.out.println("\n[채널 관리]");
            System.out.println("1. 채널 생성");
            System.out.println("2. 채널 전체 조회");
            System.out.println("3. 채널 단일 조회 (검색)");
            System.out.println("4. 채널 수정");
            System.out.println("5. 채널 삭제");
            System.out.println("0. 이전 메뉴");

            int choice = 0;
            Channel targetCh;
            String targetChNm;

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
                    System.out.print("채널 이름 입력: ");
                    targetChNm = scanner.nextLine();
                    channelService.createChannel(targetChNm);
                    break;
                case 2:
                    System.out.println("전체 채널 목록: \n" + channelService.getChannels());
                    break;
                case 3:
                    System.out.print("조회할 채널 이름 입력: ");
                    targetChNm = scanner.next();
                    scanner.nextLine();
                    targetCh = channelService.searchChannelByChannelName(targetChNm);
                    if (targetCh == null) {
                        System.out.println("존재하지 않는 채널입니다.");
                    } else {
                        System.out.println(targetCh);
                    }
                    break;
                case 4:
                    System.out.print("수정할 채널 이름 입력: ");
                    targetChNm = scanner.next();
                    scanner.nextLine();
                    targetCh = channelService.searchChannelByChannelName(targetChNm);
                    System.out.print("수정할 이름 입력: ");
                    String newChNm = scanner.nextLine();
                    channelService.updateChannel(targetCh, newChNm);
                    break;
                case 5:
                    System.out.print("삭제할 채널 이름 입력: ");
                    targetChNm = scanner.next();
                    scanner.nextLine();
                    targetCh = channelService.searchChannelByChannelName(targetChNm);
                    if (targetCh == null) {
                        System.out.println("존재하지 않는 채널입니다.");
                    } else {
                        channelService.deleteChannel(targetCh.getId());
                    }
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
