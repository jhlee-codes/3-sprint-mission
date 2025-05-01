package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;

    public BasicChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    /**
     * 채널명을 인자로 받아 채널을 생성하는 메서드
     *
     * @param channelName 생성할 채널명
     * @return 생성된 채널
     * @throws IllegalArgumentException 중복 이름인 채널이 존재하는 경우
     */
    @Override
    public Channel createChannel(String channelName) {
        if (channelRepository.findByChannelName(channelName).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 채널입니다. 다른 채널명을 입력해주세요.");
        };
        // 채널 생성
        Channel ch = new Channel(channelName);
        channelRepository.save(ch);
        return ch;
    }

    /**
     * 레포지토리에서 읽어온 채널 데이터를 리턴하는 메서드
     *
     * @return 저장된 채널 데이터
     */
    @Override
    public Map<UUID, Channel> getChannels() {
        return channelRepository.findAll();
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
        return channelRepository.findById(channelId)
                .orElseThrow(()->new NoSuchElementException("해당 ID의 채널이 존재하지 않습니다."));
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
        return channelRepository.findByChannelName(channelName)
                .orElseThrow(()->new NoSuchElementException("해당 이름의 채널이 존재하지 않습니다."));
    }

    /**
     * 주어진 채널을 새로운 채널명으로 수정하는 메서드
     *
     * @param channelId 수정할 대상 채널 ID
     * @param channelName 새로운 채널명
     * @return 수정된 채널
     */
    @Override
    public Channel updateChannel(UUID channelId, String channelName) {
        Channel ch = getChannelById(channelId);
        // 채널 수정
        ch.updateChannelName(channelName);
        channelRepository.save(ch);
        return ch;
    }

    /**
     * 주어진 id에 해당하는 채널을 삭제하는 메서드
     *
     * @param channelId 삭제할 대상 채널 id
     * @return 삭제된 채널
     */
    @Override
    public Channel deleteChannel(UUID channelId) {
        Channel targetChannel = getChannelById(channelId);
        // 채널 삭제
        channelRepository.delete(channelId);
        return targetChannel;
    }

}
