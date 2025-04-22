package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class JCFChannelService implements ChannelService {
    private final Map<UUID, Channel> data = new HashMap<>();

    /**
     * 채널명을 인자로 받아 채널을 생성하는 메서드
     *
     * @param channelName 생성할 채널명
     * @return 생성된 채널
     * @throws IllegalArgumentException 중복 이름인 채널이 존재하는 경우
     */
    @Override
    public Channel createChannel(String channelName) {
        // 중복 이름인 채널 생성 불가
        for (Channel channel : data.values()) {
            if (channel.getChannelName().equals(channelName)) {
                throw new IllegalArgumentException("이미 존재하는 채널입니다. 다른 채널명을 입력해주세요.");
            }
        }
        // 채널 생성 및 컬렉션에 추가
        Channel ch = new Channel(channelName);
        data.put(ch.getId(), ch);
        return ch;
    }

    /**
     * 메모리에 저장되어있는 채널 데이터를 리턴하는 메서드
     *
     * @return 메모리에 저장된 채널데이터
     */
    @Override
    public Map<UUID, Channel> getChannels() {
        return data;
    }

    /**
     * 주어진 id에 해당하는 채널을 조회하는 메서드
     *
     * @param channelId 조회할 채널의 ID
     * @return 조회된 채널
     * @throws NoSuchElementException 해당 ID의 채널이 존재하지 않는 경우
     */
    @Override
    public Channel getChannelById(UUID channelId) {
        Channel ch = data.get(channelId);
        if (ch == null) {
            throw new NoSuchElementException("해당 ID의 채널이 존재하지 않습니다.");
        }
        return ch;
    }

    /**
     * 주어진 채널명에 해당하는 채널을 조회하는 메서드
     *
     * @param channelName 조회할 채널명
     * @return 조회된 채널
     * @throws NoSuchElementException 해당 이름의 채널이 존재하지 않는 경우
     */
    @Override
    public Channel getChannelByChannelName(String channelName) {
        // data를 순회하며 채널명으로 검색
        return data.values().stream()
                .filter(ch -> ch.getChannelName().equals(channelName))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("해당 채널을 찾을 수 없습니다."));
    }


    /**
     * 주어진 채널을 새로운 채널명으로 수정하는 메서드
     *
     * @param channelId 수정할 대상 채널 ID
     * @param channelName 새로운 채널명
     * @return 수정된 채널
     * @throws NoSuchElementException 채널이 존재하지 않는 경우
     */
    @Override
    public Channel updateChannel(UUID channelId, String channelName) {
        Channel ch = data.get(channelId);
        // 채널 유효성 체크
        if (ch == null) {
            throw new NoSuchElementException("존재하지 않는 채널이므로 수정이 불가합니다.");
        }
        // 채널 수정
        ch.updateChannelName(channelName);
        return ch;
    }

    /**
     * 주어진 id에 해당하는 채널을 삭제하는 메서드
     *
     * @param channelId 삭제할 대상 채널 id
     * @return 삭제된 채널
     * @throws NoSuchElementException 해당 ID의 채널이 존재하지 않는 경우
     */
    @Override
    public Channel deleteChannel(UUID channelId) {
        Channel targetChannel = data.get(channelId);
        // 채널 유효성 체크
        if (targetChannel == null) {
            throw new NoSuchElementException("존재하지 않는 채널이므로 삭제가 불가합니다.");
        }
        // 채널 컬렉션에서 삭제
        data.remove(channelId);
        return targetChannel;
    }

}