package com.sprint.mission.discodeit.menu;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.Scanner;

public class userMenu {

    private final JCFUserService userService;

    public userMenu(JCFUserService userService) {
        this.userService = userService;
    }

    public void run(Scanner scanner) {
        boolean back = false;

        while (!back) {
            System.out.println("\n[유저 관리]");
            System.out.println("1. 유저 생성");
            System.out.println("2. 유저 전체 조회");
            System.out.println("3. 유저 단일 조회 (검색)");
            System.out.println("4. 유저 수정");
            System.out.println("5. 유저 삭제");
            System.out.println("0. 이전 메뉴");

            int choice = 0;
            String targetUserId;
            User targetUser;

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
                    System.out.print("유저 이름 입력: ");
                    String name = scanner.nextLine();
                    System.out.print("유저 ID 입력: ");
                    String id = scanner.nextLine();
                    userService.createUser(name,id);
                    break;
                case 2:
                    System.out.println("전체 유저 목록: \n" + userService.getUsers());
                    break;
                case 3:
                    System.out.print("조회할 유저 ID 입력: ");
                    targetUserId = scanner.next();
                    scanner.nextLine();
                    targetUser = userService.searchUserByUserId(targetUserId);
                    if (targetUser == null) {
                        System.out.println("존재하지 않는 유저입니다.");
                    } else {
                        System.out.println(targetUser);
                    }
                    break;
                case 4:
                    System.out.print("수정할 유저 ID 입력: ");
                    targetUserId = scanner.next();
                    scanner.nextLine();
                    System.out.print("수정할 이름 입력: ");
                    String newName = scanner.nextLine();
                    targetUser = userService.searchUserByUserId(targetUserId);
                    userService.updateUser(targetUser, newName);
                    break;
                case 5:
                    System.out.print("삭제할 유저 ID 입력: ");
                    targetUserId = scanner.next();
                    scanner.nextLine();
                    targetUser = userService.searchUserByUserId(targetUserId);
                    if (targetUser == null) {
                        System.out.println("존재하지 않는 유저입니다.");
                    } else {
                        userService.deleteUser(targetUser.getId());
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
