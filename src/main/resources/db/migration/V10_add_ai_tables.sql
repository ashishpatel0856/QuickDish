CREATE TABLE IF NOT EXISTS ai_conversations (
                                                id BIGSERIAL PRIMARY KEY,
                                                user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100) NOT NULL,
    feature_type VARCHAR(50) NOT NULL,
    user_message TEXT NOT NULL,
    ai_response TEXT,
    context_data JSONB,
    tokens_used INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_ai_conversations_user_id ON ai_conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_conversations_session_id ON ai_conversations(session_id);
CREATE INDEX IF NOT EXISTS idx_ai_conversations_created_at ON ai_conversations(created_at);

CREATE TABLE IF NOT EXISTS ai_cache_entries (
                                                id BIGSERIAL PRIMARY KEY,
                                                cache_key VARCHAR(255) UNIQUE NOT NULL,
    response_data TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_ai_cache_key ON ai_cache_entries(cache_key);
CREATE INDEX IF NOT EXISTS idx_ai_cache_expires ON ai_cache_entries(expires_at);

-- Rate limiting log (Free tier protection)
CREATE TABLE IF NOT EXISTS ai_rate_limit_log (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 user_id BIGINT,
                                                 ip_address VARCHAR(50),
    endpoint VARCHAR(100) NOT NULL,
    requests_count INTEGER DEFAULT 1,
    window_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    blocked BOOLEAN DEFAULT FALSE
    );

CREATE INDEX IF NOT EXISTS idx_rate_limit_user ON ai_rate_limit_log(user_id, window_start);


COMMENT ON TABLE ai_conversations IS 'Stores all AI chat history for context-aware conversations';
COMMENT ON COLUMN ai_conversations.session_id IS 'Unique session ID to group conversations. Same session_id = same chat thread';
COMMENT ON COLUMN ai_conversations.context_data IS 'JSON data like restaurant_id, order_id, location, etc.';