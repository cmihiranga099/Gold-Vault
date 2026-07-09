package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class NotificationFeedResponse {
    private List<NotificationItemResponse> items;
    private long unreadCount;
}