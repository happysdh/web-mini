package com.uniondrug.udlib.web.bean

class FormJSBean {
    /**
     * type : 1
     * name : upImage
     * uuid : 123654
     */
    var type: String? = null
    var name: String? = null
    var uuid: String? = null
    var tel: String? = null
    var base64: String? = null
    var address: String? = null
    var latitude: String? = null
    var longitude: String? = null
    var content: String? = null
    var data: List<PrescriptJSBean>? = null
    var isHasRights = false
    var roomId: String? = null
    var text: String? = null
    var id: String? = null
    //share 参数
    /**
     * shareLink : http://yaodianbao.turboradio.cn/yaodianbao/test?token=25a0f2b0-d57d-47dd-ac7c-e77caabb415d
     * shareTitle : 测试
     * shareDescr : 分享描述
     * shareImg : http://uniondrug.oss-cn-hangzhou.aliyuncs.com/tm-new-yaodianbao/yaodianbao_pic_redpacket.png
     * shareType : 1
     * shareBigImg : 0
     */
    var shareLink: String? = null
    var shareTitle: String? = null
    var shareDescr: String? = null
    var shareImg: String? = null
    var shareType: String? = null
    var shareBigImg: String? = null

}