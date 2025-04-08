package com.sprint.mission.discodeit.menu;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class userMenu {

    private final JCFUserService userService;

    public userMenu(JCFUserService userService) {
        this.userService = userService;
    }

    public void run(Scanner scanner) {
        boolean back = false;

        while (!back) {
            String targetUserId;
            User targetUser;
            int choice;

            // 유저 관리 안내 멘트 출력
            System.out.println("\n[유저 관리]");
            System.out.println("1. 유저 생성");
            System.out.println("2. 유저 전체 조회");
            System.out.println("3. 유저 단일 조회 (검색)");
            System.out.println("4. 유저 이름 수정");
            System.out.println("5. 유저 삭제");
            System.out.println("0. 이전 메뉴");

            // 입력 & 입력값이 정수가 아닌 경우 예외처리
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

            // 입력값에 따라 분기처리 진행
            try {
                switch (choice) {
                    case 1:     // 유저 생성
                        System.out.print("유저 이름 입력: ");
                        String name = scanner.next();
                        scanner.nextLine();

                        System.out.print("유저 ID 입력: ");
                        String id = scanner.next();
                        userService.createUser(name, id);
                        break;
                    case 2:     // 유저 전체 조회
                        System.out.println("전체 유저 목록: \n" + userService.getUsers());
                        break;
                    case 3:     // 유저 단일 조회 (userId로 조회)
                        System.out.print("조회할 유저 ID 입력: ");
                        targetUserId = scanner.next();
                        targetUser = userService.searchUserByUserId(targetUserId);
                        System.out.println(targetUser);
                        break;
                    case 4:     // 유저 이름 수정 (userId로 조회)
                        System.out.print("수정할 유저 ID 입력: ");
                        targetUserId = scanner.next();
                        targetUser = userService.searchUserByUserId(targetUserId);
                        scanner.nextLine();

                        System.out.print("새로운 유저명 입력: ");
                        String newName = scanner.next();
                        userService.updateUser(targetUser, newName);
                        break;
                    case 5:     // 유저 삭제 (userId로 조회)
                        System.out.print("삭제할 유저 ID 입력: ");
                        targetUserId = scanner.next();
                        targetUser = userService.searchUserByUserId(targetUserId);
                        userService.deleteUser(targetUser.getId());
                        break;
                    case 0:     // 이전 메뉴
                        back = true;
                        break;
                    default:
                        System.out.println("잘못된 선택입니다.");
                        break;
                }
            } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) { // 예외 발생시
                System.out.println(e.getMessage());
            }
        }
    }
}
