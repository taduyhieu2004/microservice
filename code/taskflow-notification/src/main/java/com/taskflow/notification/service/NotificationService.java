package com.taskflow.notification.service;

import com.taskflow.common.dto.PageResponse;
import com.taskflow.common.exception.ForbiddenException;
import com.taskflow.common.exception.NotFoundException;
import com.taskflow.notification.dto.request.UpdatePreferenceRequest;
import com.taskflow.notification.dto.response.NotificationResponse;
import com.taskflow.notification.dto.response.PreferenceResponse;
import com.taskflow.notification.entity.Notification;
import com.taskflow.notification.entity.NotificationPreference;
import com.taskflow.notification.repository.NotificationPreferenceRepository;
import com.taskflow.notification.repository.NotificationRepository;
import com.taskflow.notification.realtime.RealtimePushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final NotificationPreferenceRepository prefRepo;
    private final RealtimePushService realtime;

    @Transactional
    public Notification create(Long userId, String type, String title, String body, String link, Map<String, Object> metadata) {
        // Check preference: nếu in-app disabled cho type này, vẫn lưu DB nhưng không push realtime
        boolean push = isInAppEnabled(userId, type);

        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setLink(link);
        n.setMetadata(metadata != null ? metadata : new HashMap<>());
        n = repo.save(n);

        if (push) {
            realtime.pushToUser(userId, toResponse(n));
        }
        return n;
    }

    public PageResponse<NotificationResponse> list(Long userId, boolean unreadOnly, int page, int size) {
        var pageable = PageRequest.of(page, size);
        Page<Notification> p = unreadOnly
                ? repo.findByUserIdAndDeletedFalseAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable)
                : repo.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.of(p.map(this::toResponse));
    }

    public long unreadCount(Long userId) {
        return repo.countByUserIdAndDeletedFalseAndReadAtIsNull(userId);
    }

    @Transactional
    public NotificationResponse markRead(Long callerId, Long id) {
        Notification n = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> NotFoundException.of("Notification", id));
        if (!n.getUserId().equals(callerId)) throw new ForbiddenException("not_owner");
        if (n.getReadAt() == null) {
            n.setReadAt(Instant.now().toEpochMilli());
            n = repo.save(n);
        }
        return toResponse(n);
    }

    @Transactional
    public int markAllRead(Long callerId) {
        return repo.markAllRead(callerId, Instant.now().toEpochMilli());
    }

    @Transactional
    public void delete(Long callerId, Long id) {
        Notification n = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> NotFoundException.of("Notification", id));
        if (!n.getUserId().equals(callerId)) throw new ForbiddenException("not_owner");
        n.setDeleted(true);
        repo.save(n);
    }

    public PreferenceResponse getPreference(Long userId) {
        NotificationPreference p = prefRepo.findById(userId).orElseGet(() -> createDefault(userId));
        return toResponse(p);
    }

    @Transactional
    public PreferenceResponse updatePreference(Long userId, UpdatePreferenceRequest req) {
        NotificationPreference p = prefRepo.findById(userId).orElseGet(() -> createDefault(userId));
        if (req.getInAppEnabled() != null) p.setInAppEnabled(req.getInAppEnabled());
        if (req.getEmailEnabled() != null) p.setEmailEnabled(req.getEmailEnabled());
        if (req.getPerTypeSettings() != null) p.setPerTypeSettings(req.getPerTypeSettings());
        p.setLastUpdatedAt(Instant.now().toEpochMilli());
        return toResponse(prefRepo.save(p));
    }

    private boolean isInAppEnabled(Long userId, String type) {
        return prefRepo.findById(userId)
                .map(p -> {
                    if (Boolean.FALSE.equals(p.getInAppEnabled())) return false;
                    if (p.getPerTypeSettings() != null && p.getPerTypeSettings().containsKey(type)) {
                        return Boolean.TRUE.equals(p.getPerTypeSettings().get(type));
                    }
                    return true;
                })
                .orElse(true);
    }

    private NotificationPreference createDefault(Long userId) {
        NotificationPreference p = new NotificationPreference();
        p.setUserId(userId);
        p.setInAppEnabled(true);
        p.setEmailEnabled(false);
        p.setCreatedAt(Instant.now().toEpochMilli());
        return prefRepo.save(p);
    }

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).userId(n.getUserId()).type(n.getType())
                .title(n.getTitle()).body(n.getBody()).link(n.getLink())
                .readAt(n.getReadAt()).metadata(n.getMetadata())
                .createdAt(n.getCreatedAt()).build();
    }

    private PreferenceResponse toResponse(NotificationPreference p) {
        return PreferenceResponse.builder()
                .userId(p.getUserId())
                .inAppEnabled(p.getInAppEnabled())
                .emailEnabled(p.getEmailEnabled())
                .perTypeSettings(p.getPerTypeSettings())
                .build();
    }
}
