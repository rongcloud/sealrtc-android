package cn.rongcloud.rtc.entity;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by wangw on 2019-10-11.
 */
public class McuConfig {


    /**
     * version : 1
     * mode : 1
     * host_user_id : 001
     * output : {"video":{"normal":{"width":360,"height":640,"fps":25,"bitrate":800},"tiny":{"width":180,"height":320,"fps":15,"bitrate":200},"exparams":{"renderMode":1}},"audio":{"bitrate":200}}
     * input : {"video":[{"user_id":"111","x":0,"y":0,"width":180,"height":320},{"user_id":"2222","x":180,"y":320,"width":180,"height":320}]}
     */

    private int version = 1;
    private int mode = 3;   // 2和3不用指定 input   1. 自定义布局， 2:悬浮布局，3：自适应布局；
    private String host_user_id;    // 把主播放在 左上;
    private OutputBean output;
    private InputBean input;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getHost_user_id() {
        return host_user_id;
    }

    public void setHost_user_id(String host_user_id) {
        this.host_user_id = host_user_id;
    }

    public OutputBean getOutput() {
        return output;
    }

    public void setOutput(OutputBean output) {
        this.output = output;
    }

    public InputBean getInput() {
        return input;
    }

    public void setInput(InputBean input) {
        this.input = input;
    }

    public static class OutputBean {
        /**
         * video : {"normal":{"width":360,"height":640,"fps":25,"bitrate":800},"tiny":{"width":180,"height":320,"fps":15,"bitrate":200},"exparams":{"renderMode":1}}
         * audio : {"bitrate":200}
         */

        private VideoBean video;
        private AudioBean audio;

        public VideoBean getVideo() {
            return video;
        }

        public void setVideo(VideoBean video) {
            this.video = video;
        }

        public AudioBean getAudio() {
            return audio;
        }

        public void setAudio(AudioBean audio) {
            this.audio = audio;
        }

        public static class VideoBean {
            /**
             * normal : {"width":360,"height":640,"fps":25,"bitrate":800}
             * tiny : {"width":180,"height":320,"fps":15,"bitrate":200}
             * exparams : {"renderMode":1}
             */

            private NormalBean normal;
            private TinyBean tiny;
            private ExparamsBean exparams;

            public NormalBean getNormal() {
                return normal;
            }

            public void setNormal(NormalBean normal) {
                this.normal = normal;
            }

            public TinyBean getTiny() {
                return tiny;
            }
            public void setTiny(TinyBean tiny) {
                this.tiny = tiny;
            }

            public ExparamsBean getExparams() {
                return exparams;
            }

            public void setExparams(ExparamsBean exparams) {
                this.exparams = exparams;
            }

            public static class NormalBean {
                /**
                 * width : 360
                 * height : 640
                 * fps : 25
                 * bitrate : 800
                 */

                private int width;
                private int height;
                private int fps;
                private int bitrate;

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getFps() {
                    return fps;
                }

                public void setFps(int fps) {
                    this.fps = fps;
                }

                public int getBitrate() {
                    return bitrate;
                }

                public void setBitrate(int bitrate) {
                    this.bitrate = bitrate;
                }
            }

            public static class TinyBean {
                /**
                 * width : 180
                 * height : 320
                 * fps : 15
                 * bitrate : 200
                 */

                private int width;
                private int height;
                private int fps;
                private int bitrate;

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getFps() {
                    return fps;
                }

                public void setFps(int fps) {
                    this.fps = fps;
                }

                public int getBitrate() {
                    return bitrate;
                }

                public void setBitrate(int bitrate) {
                    this.bitrate = bitrate;
                }
            }

            public static class ExparamsBean {
                /**
                 * renderMode : 1
                 */

                private int renderMode = 1; // 1:crop裁剪填充 ；2:whole

                public int getRenderMode() {
                    return renderMode;
                }

                public void setRenderMode(int renderMode) {
                    this.renderMode = renderMode;
                }
            }
        }

        public static class AudioBean {
            /**
             * bitrate : 200
             */

            private int bitrate;

            public int getBitrate() {
                return bitrate;
            }

            public void setBitrate(int bitrate) {
                this.bitrate = bitrate;
            }
        }
    }

    public static class InputBean {
        private List<VideoBeanX> video;

        public List<VideoBeanX> getVideo() {
            return video;
        }

        public void setVideo(List<VideoBeanX> video) {
            this.video = video;
        }

        public static class VideoBeanX {
            /**
             * user_id : 111
             * x : 0
             * y : 0
             * width : 180
             * height : 320
             */

            private String user_id;
            private int x;
            private int y;
            private int width;
            private int height;

            public String getUser_id() {
                return user_id;
            }

            public void setUser_id(String user_id) {
                this.user_id = user_id;
            }

            public int getX() {
                return x;
            }

            public void setX(int x) {
                this.x = x;
            }

            public int getY() {
                return y;
            }

            public void setY(int y) {
                this.y = y;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }
        }
    }
}
