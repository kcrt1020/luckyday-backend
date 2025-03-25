package com.example.luckydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowId implements Serializable {
    private Long followerId;
    private Long followingId;

    // equals, hashCode 구현 필수
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FollowId followId = (FollowId) o;

        if (!followerId.equals(followId.followerId)) return false;
        return followingId.equals(followId.followingId);
    }

    @Override
    public int hashCode() {
        int result = followerId.hashCode();
        result = 31 * result + followingId.hashCode();
        return result;
    }
}
