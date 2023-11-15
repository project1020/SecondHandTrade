package com.secondhand.trade

data class articles (
    var title: String?= null,
    var content: String?= null,
    var price: Int?= null,
    var imageUrl: String?= null,
    var isSoldOut: Boolean?= null
)

class ActivityArticle {
    // db 연결, 및 ui 연동


    // db에 설정한 값 등록하는 함수


    // db에 수정한 값 적용하는 update 함수


}