package com.sprint.mission.discodeit.fixture;

import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class BinaryContentFixture {

//    public static BinaryContent createAttachment(String name, Long size, String type) {
//        BinaryContent attachment = new BinaryContent(name, size, type);
//        ReflectionTestUtils.setField(attachment, "id", UUID.randomUUID());
//        return attachment;
//    }
//
//    public static BinaryContent createProfile(String fileName, Long size, String contentType) {
//        BinaryContent profile = new BinaryContent(fileName, size, contentType);
//        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
//        return profile;
//    }

    public static BinaryContent createBinaryContent(String fileName, Long size,
            String contentType) {
        BinaryContent profile = new BinaryContent(fileName, size, contentType);
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
        return profile;
    }

    public static BinaryContentDto createBinaryContentDto(BinaryContent attachment) {
        return new BinaryContentDto(attachment.getId(), attachment.getFileName(),
                attachment.getSize(), attachment.getContentType());
    }
}
