(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-0e61d787"],{"75ce":function(t,e,i){},b04d:function(t,e,i){"use strict";i.r(e);var a=function(){var t=this,e=t.$createElement,i=t._self._c||e;return i("div",{staticClass:"page"},[i("div",{staticClass:"info-title"},[t._m(0),i("img",{attrs:{src:t.item.memberIcon?t.item.memberIcon:"https://uniondrug-release.oss-cn-shanghai.aliyuncs.com/backend.app/2h80j6hdfu3o35tb3evlj9f3as.png",alt:""},on:{click:t.upImage}})]),i("div",{staticClass:"list"},[i("div",{staticClass:"item"},[i("p",[t._v("真实姓名")]),i("p",{staticClass:"p"},[t._v(t._s(t.item.memberName))])]),i("div",{staticClass:"item"},[i("p",[t._v("性别")]),i("van-radio-group",{staticClass:"title title2",attrs:{disabled:"",direction:"horizontal"},model:{value:t.item.sex,callback:function(e){t.$set(t.item,"sex",e)},expression:"item.sex"}},[i("van-radio",{attrs:{"checked-color":"#00B095",name:"1"}},[t._v("男")]),i("van-radio",{attrs:{"checked-color":"#00B095",name:"2"}},[t._v("女")])],1)],1),i("div",{staticClass:"item",on:{click:t.UDToRealNameAuthentication}},[i("p",[t._v("实名认证")]),i("div",["0"==t.item.isCertification?i("p",[t._v("未认证")]):t._e(),"1"==t.item.isCertification?i("p",[t._v("已认证")]):t._e(),i("van-icon",{attrs:{name:"arrow"}})],1)]),i("div",{staticClass:"item",on:{click:t.UDToPrivilegeAttestation}},[i("p",[t._v("药师认证")]),i("div",[i("p",[t._v(t._s(t.isCertificationFn(t.item.pharmacistStatus)))]),i("van-icon",{attrs:{name:"arrow"}})],1)]),i("div",{staticClass:"item"},[i("p",[t._v("头衔")]),i("input",{directives:[{name:"model",rawName:"v-model",value:t.item.memberTitle,expression:"item.memberTitle"}],attrs:{placeholder:"请输入头衔",type:"text"},domProps:{value:t.item.memberTitle},on:{input:function(e){e.target.composing||t.$set(t.item,"memberTitle",e.target.value)}}})]),i("div",{staticClass:"item"},[i("p",[t._v("任职单位")]),i("input",{directives:[{name:"model",rawName:"v-model",value:t.item.workPlace,expression:"item.workPlace"}],attrs:{placeholder:"请输入任职单位",type:"text"},domProps:{value:t.item.workPlace},on:{input:function(e){e.target.composing||t.$set(t.item,"workPlace",e.target.value)}}})]),i("div",{staticClass:"item"},[i("p",[t._v("个人简介")]),i("input",{directives:[{name:"model",rawName:"v-model",value:t.item.personProfile,expression:"item.personProfile"}],attrs:{placeholder:"请输入个人简介",type:"text"},domProps:{value:t.item.personProfile},on:{input:function(e){e.target.composing||t.$set(t.item,"personProfile",e.target.value)}}})]),i("div",{staticClass:"item",on:{click:function(e){return t.qianList()}}},[i("p",[t._v("授课协议")]),i("div",[i("p",[t._v(t._s("0"==t.item.protocolStatus?"未签署":"已签署"))]),i("van-icon",{attrs:{name:"arrow"}})],1)])]),i("div",{staticClass:"footer"},[i("button",{staticClass:"btn",on:{click:t.getData}},[t._v("确认")])])])},n=[function(){var t=this,e=t.$createElement,i=t._self._c||e;return i("div",[i("h3",[t._v("我的头像")]),i("p",[t._v("请上传身穿白大褂真实照片作为头像")])])}],s={name:"inofUp",data:function(){return{item:{}}},created:function(){this.info()},methods:{isCertificationFn:function(t){return"0"==t?"未认证":"1"==t?"待审核":"2"==t?"已认证":"3"==t?"认证失败":void 0},UDToPrivilegeAttestation:function(){sessionStorage.removeItem("login"),this.$bs.UDToPrivilegeAttestation({}).then((function(){}))},qianList:function(){this.$router.push("qianList")},upImage:function(){var t=this;this.$bs.upImage({type:"1"}).then((function(e){console.log(e),"1"==e.status?(t.$toast("上传成功"),t.item.memberIcon=e.data.data):e.msg&&t.$toast(e.msg)}))},afterRead:function(t){var e=this;console.log(t);var i=new FormData;i.append("file",t.file),this.$api.upload(i).then((function(t){e.item.memberIcon=t.url}))},UDToRealNameAuthentication:function(){sessionStorage.removeItem("login"),this.$bs.UDToRealNameAuthentication({}).then((function(){}))},info:function(){var t=this;this.$api.info().then((function(e){t.item=e}))},getData:function(){this.$api.updateInfo(this.item).then((function(t){history.back()}))}}},o=s,r=(i("f6e9"),i("2877")),c=Object(r["a"])(o,a,n,!1,null,"18409bce",null);e["default"]=c.exports},f6e9:function(t,e,i){"use strict";var a=i("75ce"),n=i.n(a);n.a}}]);
//# sourceMappingURL=chunk-0e61d787.b06df59e.js.map