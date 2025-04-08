package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.UUID;


public class JCFUserService implements UserService {
    private final List<User> data;

    public JCFUserService(List<User> data) {
        this.data = data;
    }

    @Override
    public User createUser(String userName, String userId) {
        // 중복 이름인 유저 생성 불가
        for (User user : data) {
            if (user.getUserId().equals(userId)) {
                System.out.println("이미 존재하는 ID입니다. 다른 ID를 입력해주세요.");
                return null;
            }
        }

        // 유저 생성
        User user = new User(userName, userId);

        // 유저 컬렉션에 추가
        data.add(user);
        System.out.println("유저 생성 ) " + userName + "("+ userId+")"+" 등록되었습니다.");
        return user;
    }

    @Override
    public User getUser(UUID id) {
        return data.stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<User> getUsers() {
        return data;
    }

    @Override
    public User updateUser(User user, String userName) {
        // 유저 유효성 체크
        if (user == null || !data.contains(user)) {
            System.out.println("존재하지 않는 유저이기 때문에, 수정이 불가합니다.");
            return null;
        }

        String beforeUserName = user.getUserName();

        // 유저 이름 수정
        for (User u : data) {
            if (u.getId().equals(user.getId())) {
                u.updateUserName(userName);
                break;
            }
        }
        System.out.println("유저 수정 ) " + beforeUserName + " -> " + userName + "("+user.getUserId()+")"+"으로 수정되었습니다.");
        return user;
    }

    @Override
    public void deleteUser(UUID id) {
        User targetUser = getUser(id);
        String targetUserName = targetUser.getUserName();

        // 유저 삭제시 채널, 메시지 상의 유저는 사라지지 않고 (탈퇴) 라고 표시
        // 유저 isActive값 설정 (false)
        targetUser.updateIsActive();

        // 유저 삭제
        data.remove(targetUser);
        System.out.println("유저 삭제 ) " + targetUserName + "가 삭제되었습니다.");
    }

    @Override
    public User searchUserByUserId(String userId) {
        // data를 순회하며 유저 ID로 검색
        for (User user : data) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

}