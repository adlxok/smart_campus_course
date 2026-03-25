<script setup lang="ts">
import { ref, nextTick, computed } from 'vue'
import axios from 'axios'
import { marked } from 'marked'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const isOpen = ref(false)
const isLoading = ref(false)
const inputMessage = ref('')
const messages = ref<Message[]>([
  {
    role: 'assistant',
    content: '你好！我是智慧学堂的智能小助手小智~ 有什么可以帮你的吗？'
  }
])
const chatContainer = ref<HTMLElement | null>(null)

const toggleChat = () => {
  isOpen.value = !isOpen.value
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || isLoading.value) return

  messages.value.push({ role: 'user', content })
  inputMessage.value = ''
  scrollToBottom()

  isLoading.value = true

  try {
    const history = messages.value
      .slice(-10)
      .map(m => ({ role: m.role, content: m.content }))

    const { data } = await axios.post('http://localhost:8080/api/ai-assistant/chat', {
      message: content,
      history
    })

    if (data.success) {
      messages.value.push({ role: 'assistant', content: data.content })
    } else {
      messages.value.push({ role: 'assistant', content: data.error || '抱歉，服务暂时不可用~' })
    }
  } catch {
    messages.value.push({ role: 'assistant', content: '网络连接失败，请检查网络后重试~' })
  }

  isLoading.value = false
  scrollToBottom()
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const clearMessages = () => {
  messages.value = [
    {
      role: 'assistant',
      content: '你好！我是智慧学堂的智能小助手小智~ 有什么可以帮你的吗？'
    }
  ]
}

const renderMarkdown = (text: string) => {
  if (!text) return ''
  return marked(text, { breaks: true, gfm: true })
}
</script>

<template>
  <div class="ai-assistant">
    <!-- 浮动按钮 -->
    <div class="float-btn" :class="{ active: isOpen }" @click="toggleChat">
      <svg v-if="!isOpen" viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        <circle cx="9" cy="10" r="1" fill="currentColor"/>
        <circle cx="12" cy="10" r="1" fill="currentColor"/>
        <circle cx="15" cy="10" r="1" fill="currentColor"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="18" y1="6" x2="6" y2="18"/>
        <line x1="6" y1="6" x2="18" y2="18"/>
      </svg>
    </div>

    <!-- 聊天面板 -->
    <Transition name="slide-up">
      <div v-if="isOpen" class="chat-panel">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-left">
            <div class="avatar-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
              </svg>
            </div>
            <span class="header-title">智能小助手</span>
          </div>
          <button class="clear-btn" @click="clearMessages" title="清空对话">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="1 4 1 10 7 10"/>
              <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>
            </svg>
          </button>
        </div>

        <!-- 消息列表 -->
        <div ref="chatContainer" class="chat-messages">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message"
            :class="msg.role"
          >
            <div v-if="msg.role === 'assistant'" class="msg-avatar">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="#409eff">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
              </svg>
            </div>
            <div class="msg-bubble">
              <div v-if="msg.role === 'assistant'" class="markdown-content" v-html="renderMarkdown(msg.content)"></div>
              <div v-else class="user-content">{{ msg.content }}</div>
              <span v-if="msg.role === 'assistant' && isLoading && index === messages.length - 1 && !msg.content" class="typing-dots">
                <span></span><span></span><span></span>
              </span>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
          <textarea
            v-model="inputMessage"
            placeholder="输入你的问题..."
            :disabled="isLoading"
            @keydown="handleKeydown"
            rows="1"
          ></textarea>
          <button
            class="send-btn"
            :disabled="!inputMessage.trim() || isLoading"
            @click="sendMessage"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.ai-assistant {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 2000;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 浮动按钮 */
.float-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  transition: all 0.3s ease;
}

.float-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(64, 158, 255, 0.5);
}

.float-btn.active {
  background: linear-gradient(135deg, #f56c6c, #f78989);
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.4);
}

.float-btn.active:hover {
  box-shadow: 0 6px 24px rgba(245, 108, 108, 0.5);
}

/* 聊天面板 */
.chat-panel {
  position: absolute;
  bottom: 72px;
  right: 0;
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
}

.clear-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.clear-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f8f9fa;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 2px;
}

.message {
  display: flex;
  gap: 8px;
  max-width: 100%;
}

.message.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  position: relative;
  min-width: 0;
}

.markdown-content {
  color: #303133;
  max-width: 100%;
  overflow-wrap: break-word;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3) {
  margin-top: 12px;
  margin-bottom: 8px;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-content :deep(h1) {
  font-size: 16px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 4px;
}

.markdown-content :deep(h2) {
  font-size: 15px;
}

.markdown-content :deep(h3) {
  font-size: 14px;
}

.markdown-content :deep(p) {
  margin: 6px 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 6px 0;
  padding-left: 20px;
}

.markdown-content :deep(li) {
  margin: 4px 0;
}

.markdown-content :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Courier New', monospace;
  color: #e6a23c;
}

.markdown-content :deep(pre) {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
  font-size: 13px;
}

.markdown-content :deep(strong) {
  font-weight: 600;
}

.markdown-content :deep(blockquote) {
  border-left: 3px solid #409eff;
  padding-left: 12px;
  color: #606266;
  margin: 8px 0;
}

.user-content {
  white-space: pre-wrap;
  word-break: break-word;
}

.message.assistant .msg-bubble {
  background: #fff;
  color: #303133;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.message.user .msg-bubble {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  border-top-right-radius: 4px;
}

/* 打字动画 */
.typing-dots {
  display: inline-flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 输入区域 */
.chat-input-area {
  display: flex;
  align-items: flex-end;
  padding: 12px 14px;
  gap: 10px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}

.chat-input-area textarea {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  padding: 8px 12px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  outline: none;
  line-height: 1.5;
  max-height: 80px;
  transition: border-color 0.2s;
}

.chat-input-area textarea:focus {
  border-color: #409eff;
}

.chat-input-area textarea:disabled {
  background: #f5f7fa;
  cursor: not-allowed;
}

.send-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #337ecc, #409eff);
  transform: scale(1.05);
}

.send-btn:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

/* 过渡动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.95);
}

/* 响应式 */
@media (max-width: 480px) {
  .ai-assistant {
    bottom: 16px;
    right: 16px;
  }

  .chat-panel {
    width: calc(100vw - 32px);
    height: 460px;
  }
}
</style>
