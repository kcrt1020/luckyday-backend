package com.example.luckydaybackend.service;

import com.example.luckydaybackend.model.Tweet;
import com.example.luckydaybackend.repository.TweetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TweetService {
    private final TweetRepository tweetRepository;

    public TweetService(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    public Tweet createTweet(Tweet tweet) {
        return tweetRepository.save(tweet);
    }

    public List<Tweet> getAllTweets() {
        return tweetRepository.findAllByOrderByCreatedAtDesc();
    }

    public Tweet getTweetById(Long id) {
        return tweetRepository.findById(id).orElse(null);
    }

    public void deleteTweet(Long id) {
        tweetRepository.deleteById(id);
    }
}