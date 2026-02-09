/**
 * 用户访问统计前端SDK
 * 实现页面访问记录和心跳检测功能
 */

class VisitTracker {
    constructor(options = {}) {
        this.options = {
            apiUrl: '/system/visit',
            userId: null,
            webId: 'default',
            pageUrl: window.location.href,
            pageTitle: document.title,
            heartbeatInterval: 1000, // 心跳间隔1秒
            maxDuration: 5 * 60, // 最大记录时长5分钟
            ...options
        };
        
        this.visitId = null;
        this.startTime = null;
        this.duration = 0;
        this.heartbeatTimer = null;
        this.isActive = false;
        this.init();
    }

    init() {
        // 页面加载时记录访问
        this.recordVisit();
        
        // 监听页面活动事件
        this.bindActivityEvents();
        
        // 开始心跳检测
        this.startHeartbeat();
        
        // 页面卸载时清理
        window.addEventListener('beforeunload', () => {
            this.stopHeartbeat();
            this.sendFinalHeartbeat();
        });
    }

    async recordVisit() {
        try {
            const response = await fetch(`${this.options.apiUrl}/record`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                    userId: this.options.userId,
                    webId: this.options.webId,
                    pageUrl: this.options.pageUrl,
                    pageTitle: this.options.pageTitle
                })
            });
            
            const result = await response.json();
            if (result.code === 200) {
                this.visitId = result.data;
                this.startTime = Date.now();
                this.isActive = true;
                console.log('访问记录成功，visitId:', this.visitId);
            }
        } catch (error) {
            console.error('访问记录失败:', error);
        }
    }

    bindActivityEvents() {
        const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
        let activityTimeout;

        const resetActivityTimeout = () => {
            clearTimeout(activityTimeout);
            this.isActive = true;
            
            activityTimeout = setTimeout(() => {
                this.isActive = false;
            }, 30000); // 30秒无操作认为不活跃
        };

        events.forEach(event => {
            document.addEventListener(event, resetActivityTimeout, true);
        });

        // 页面可见性变化监听
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.isActive = false;
            } else {
                this.isActive = true;
                resetActivityTimeout();
            }
        });
    }

    startHeartbeat() {
        this.heartbeatTimer = setInterval(() => {
            if (this.isActive && this.visitId) {
                this.duration = Math.floor((Date.now() - this.startTime) / 1000);
                
                // 超过5分钟停止记录
                if (this.duration >= this.options.maxDuration) {
                    this.stopHeartbeat();
                    return;
                }
                
                this.sendHeartbeat(this.duration);
            }
        }, this.options.heartbeatInterval);
    }

    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }

    async sendHeartbeat(duration) {
        try {
            await fetch(`${this.options.apiUrl}/heartbeat/${this.visitId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                    duration: duration
                })
            });
        } catch (error) {
            console.error('心跳发送失败:', error);
        }
    }

    sendFinalHeartbeat() {
        if (this.visitId && this.startTime) {
            const finalDuration = Math.floor((Date.now() - this.startTime) / 1000);
            this.sendHeartbeat(Math.min(finalDuration, this.options.maxDuration));
        }
    }

    // 获取今日统计
    async getTodayStats() {
        try {
            const response = await fetch(`${this.options.apiUrl}/today-stats?userId=${this.options.userId}&webId=${this.options.webId}`);
            return await response.json();
        } catch (error) {
            console.error('获取今日统计失败:', error);
            return null;
        }
    }

    // 获取月度统计
    async getMonthlyStats() {
        try {
            const response = await fetch(`${this.options.apiUrl}/monthly-stats?userId=${this.options.userId}&webId=${this.options.webId}`);
            return await response.json();
        } catch (error) {
            console.error('获取月度统计失败:', error);
            return null;
        }
    }
}

// 初始化访问追踪
window.VisitTracker = VisitTracker;

// 使用示例：
// const tracker = new VisitTracker({
//     userId: 123, // 用户ID
//     webId: 'website1', // 网站ID
//     apiUrl: '/api/system/visit' // API基础路径
// });