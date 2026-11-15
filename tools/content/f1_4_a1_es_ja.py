"""F1.4 — A1 completo de ES→JA (150 lecciones nuevas, 15 bloques)."""

from __future__ import annotations

import importlib.util
import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json"
COURSE_ID = "es-ja"
NEXT_LEVEL = "es-ja-lv-a2"

SPEC = importlib.util.spec_from_file_location("f1_1", ROOT / "tools/content/f1_1_a1_es_en.py")
F1_1 = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(F1_1)


def L(title: str, topic: str, items: list[tuple[str, str]]) -> dict:
    return {"title": title, "topic": topic, "items": items}


JA_BLOCKS = [
    (
        "はじめに",
        [
            L("あいさつ", "あいさつ", [("こんにちは", "hello"), ("さようなら", "goodbye"), ("ありがとう", "thank you")]),
            L("数字", "数字", [("一冊", "one book"), ("二冊", "two books"), ("三冊", "three books")]),
            L("色", "色", [("赤", "red"), ("青", "blue"), ("緑", "green")]),
            L("教室", "教室", [("本", "a book"), ("椅子", "a chair"), ("鉛筆", "a pencil")]),
            L("家族", "家族", [("兄", "a brother"), ("姉", "a sister"), ("息子", "a son")]),
            L("職業", "職業", [("先生", "teacher"), ("医者", "doctor"), ("学生", "student")]),
            L("ペット", "ペット", [("犬", "a dog"), ("猫", "a cat"), ("鳥", "a bird")]),
            L("食べ物", "食べ物", [("ピザ", "pizza"), ("果物", "fruit"), ("パン", "bread")]),
            L("場所", "場所", [("公園", "the park"), ("学校", "the school"), ("病院", "the hospital")]),
            L("説明", "説明", [("公園", "the park"), ("博物館", "the museum"), ("大きい", "big")]),
        ],
    ),
    (
        "私の一日",
        [
            L("日課", "日課", [("七時 に 起きます", "wake up at seven"), ("八時 に 朝ごはん を 食べます", "have breakfast at eight"), ("十時 に 寝ます", "go to bed at ten")]),
            L("時間", "時間", [("一時", "one o'clock"), ("二時", "two o'clock"), ("三時", "three o'clock")]),
            L("家", "家", [("台所", "the kitchen"), ("お風呂", "the bathroom"), ("居間", "the living room")]),
            L("家の物", "家の物", [("ベッド", "a bed"), ("テーブル", "a table"), ("窓", "a window")]),
            L("朝ごはん", "朝ごはん", [("卵", "an egg"), ("パン", "bread"), ("りんご", "an apple")]),
            L("飲み物", "飲み物", [("水", "water"), ("コーヒー", "coffee"), ("牛乳", "milk")]),
            L("家事", "家事", [("料理する", "cook"), ("掃除する", "clean"), ("洗濯する", "wash")]),
            L("自由時間", "自由時間", [("読む", "reading"), ("踊る", "dancing"), ("歌う", "singing")]),
            L("買い物", "買い物", [("シャツ", "a shirt"), ("靴", "shoes"), ("帽子", "a hat")]),
            L("町", "町", [("公園", "the park"), ("博物館", "the museum"), ("美しい", "beautiful")]),
        ],
    ),
    (
        "人",
        [
            L("家族", "家族", [("父", "a father"), ("母", "a mother"), ("祖父", "a grandfather")]),
            L("友達", "友達", [("友達", "a friend"), ("女友達", "a female friend"), ("隣人", "a neighbour")]),
            L("年齢", "年齢", [("十", "ten"), ("二十", "twenty"), ("三十", "thirty")]),
            L("外見", "外見", [("髪", "the hair"), ("腕", "the arm"), ("長い", "long")]),
            L("性格", "性格", [("先生", "the teacher"), ("学生", "the student"), ("親切", "kind")]),
            L("服", "服", [("シャツ", "a shirt"), ("ズボン", "trousers"), ("コート", "a coat")]),
            L("仕事", "仕事", [("料理人", "cook"), ("運転手", "driver"), ("店員", "shop assistant")]),
            L("オフィス", "オフィス", [("オフィス", "the office"), ("店", "the shop"), ("工房", "the workshop")]),
            L("学校", "学校", [("机", "a table"), ("コンピューター", "a computer"), ("ノート", "a notebook")]),
            L("復習", "復習", [("本", "a book"), ("リュック", "a backpack"), ("ペン", "a pen")]),
        ],
    ),
    (
        "食べ物",
        [
            L("果物", "果物", [("りんご", "an apple"), ("バナナ", "a banana"), ("オレンジ", "an orange")]),
            L("野菜", "野菜", [("にんじん", "a carrot"), ("トマト", "a tomato"), ("玉ねぎ", "an onion")]),
            L("飲み物", "飲み物", [("水", "water"), ("ジュース", "juice"), ("お茶", "tea")]),
            L("朝ごはん", "朝ごはん", [("パン", "bread"), ("卵", "an egg"), ("シリアル", "cereal")]),
            L("昼ごはん", "昼ごはん", [("ご飯", "rice"), ("鶏肉", "chicken"), ("魚", "fish")]),
            L("レストラン", "レストラン", [("テーブル", "a table"), ("メニュー", "a menu"), ("お会計", "the bill")]),
            L("好み", "好み", [("チョコレート", "chocolate"), ("チーズ", "cheese"), ("スープ", "soup")]),
            L("市場", "市場", [("りんご", "an apple"), ("トマト", "a tomato"), ("水 の ボトル", "a bottle of water")]),
            L("値段", "値段", [("五百円", "five euros"), ("千円", "ten euros"), ("二千円", "twenty euros")]),
            L("復習", "復習", [("レストラン", "the restaurant"), ("メニュー", "the menu"), ("高い", "expensive")]),
        ],
    ),
    (
        "家と町",
        [
            L("部屋", "部屋", [("寝室", "the bedroom"), ("台所", "the kitchen"), ("お風呂", "the bathroom")]),
            L("家具", "家具", [("ベッド", "a bed"), ("椅子", "a chair"), ("ソファ", "a sofa")]),
            L("通り", "通り", [("通り", "the street"), ("広場", "the square"), ("スーパー", "the supermarket")]),
            L("方向", "方向", [("左", "left"), ("右", "right"), ("まっすぐ", "straight")]),
            L("交通", "交通", [("バス", "by bus"), ("電車", "by train"), ("自転車", "by bike")]),
            L("駅", "駅", [("駅", "the station"), ("空港", "the airport"), ("ホテル", "the hotel")]),
            L("店", "店", [("パン", "bread"), ("牛乳", "milk"), ("シャツ", "a shirt")]),
            L("近所", "近所", [("近所", "the neighbourhood"), ("公園", "the park"), ("静か", "quiet")]),
            L("私の家", "私の家", [("家", "a house"), ("アパート", "a flat"), ("部屋", "a room")]),
            L("復習", "復習", [("鍵", "a key"), ("地図", "a map"), ("切符", "a ticket")]),
        ],
    ),
    (
        "健康",
        [
            L("体", "体", [("頭", "head"), ("手", "hand"), ("足", "foot")]),
            L("症状", "症状", [("頭 が 痛い です", "a headache"), ("お腹 が 痛い です", "a stomachache"), ("熱 が あります", "a fever")]),
            L("医者", "医者", [("予約", "an appointment"), ("医者", "a doctor"), ("助け", "help")]),
            L("薬局", "薬局", [("薬", "medicine"), ("包帯", "a bandage"), ("錠剤", "pills")]),
            L("健康的な習慣", "健康的な習慣", [("歩く", "walk"), ("休む", "rest"), ("飲む", "drink")]),
            L("スポーツ", "スポーツ", [("サッカー", "football"), ("テニス", "tennis"), ("バスケットボール", "basketball")]),
            L("睡眠", "睡眠", [("寝る", "sleep"), ("夢見る", "dream"), ("呼吸する", "breathe")]),
            L("ジム", "ジム", [("ジム", "the gym"), ("プール", "the pool"), ("公園", "the park")]),
            L("緊急", "緊急", [("医者", "a doctor"), ("救急車", "an ambulance"), ("助け", "help")]),
            L("復習", "復習", [("医者", "the doctor"), ("病院", "the hospital"), ("親切", "kind")]),
        ],
    ),
    (
        "学校と仕事",
        [
            L("科目", "科目", [("歴史", "history"), ("科学", "science"), ("芸術", "art")]),
            L("教室", "教室", [("本", "a book"), ("ノート", "a notebook"), ("定規", "a ruler")]),
            L("学校", "学校", [("学校", "the school"), ("図書館", "the library"), ("教室", "the classroom")]),
            L("職業", "職業", [("先生", "teacher"), ("看護師", "nurse"), ("技師", "engineer")]),
            L("オフィス", "オフィス", [("オフィス", "the office"), ("会議", "the meeting"), ("机", "the desk")]),
            L("仕事", "仕事", [("書く", "write"), ("読む", "read"), ("話す", "speak")]),
            L("予定", "予定", [("八時", "eight o'clock"), ("十時", "ten o'clock"), ("十二時", "twelve o'clock")]),
            L("道具", "道具", [("コンピューター", "a computer"), ("携帯電話", "a phone"), ("フォルダ", "a folder")]),
            L("仕事", "仕事", [("働きます", "work"), ("勉強します", "study"), ("休みます", "rest")]),
            L("復習", "復習", [("仕事", "the work"), ("プロジェクト", "the project"), ("大切", "important")]),
        ],
    ),
    (
        "買い物",
        [
            L("服", "服", [("シャツ", "a shirt"), ("ドレス", "a dress"), ("靴", "shoes")]),
            L("色", "色", [("赤", "red"), ("青", "blue"), ("黒", "black")]),
            L("サイズ", "サイズ", [("小さい サイズ", "a small size"), ("中 サイズ", "a medium size"), ("大きい サイズ", "a large size")]),
            L("店", "店", [("店", "the shop"), ("ショッピングセンター", "the shopping centre"), ("市場", "the market")]),
            L("値段", "値段", [("五百円", "five euros"), ("千五百円", "fifteen euros"), ("五千円", "fifty euros")]),
            L("支払う", "支払う", [("払う", "pay"), ("貯金する", "save"), ("使う", "spend")]),
            L("贈り物", "贈り物", [("贈り物", "a gift"), ("花", "flowers"), ("カード", "a card")]),
            L("好み", "好み", [("服", "clothes"), ("青い 色", "the colour blue"), ("綿", "cotton")]),
            L("市場", "市場", [("りんご", "an apple"), ("パン", "bread"), ("チーズ", "cheese")]),
            L("復習", "復習", [("市場", "the market"), ("ショッピングセンター", "the shopping centre"), ("大きい", "big")]),
        ],
    ),
    (
        "旅行",
        [
            L("交通", "交通", [("飛行機", "by plane"), ("電車", "by train"), ("車", "by car")]),
            L("駅", "駅", [("駅", "the station"), ("空港", "the airport"), ("バス停", "the stop")]),
            L("ホテル", "ホテル", [("部屋", "a room"), ("鍵", "a key"), ("地図", "a map")]),
            L("荷物", "荷物", [("スーツケース", "a suitcase"), ("パスポート", "a passport"), ("切符", "a ticket")]),
            L("方向", "方向", [("左", "left"), ("右", "right"), ("まっすぐ", "straight")]),
            L("休暇", "休暇", [("旅行する", "travel"), ("泳ぐ", "swim"), ("訪ねる", "visit")]),
            L("天気", "天気", [("晴れ", "sunny"), ("雨", "rainy"), ("曇り", "cloudy")]),
            L("ビーチ", "ビーチ", [("ビーチ", "the beach"), ("海", "the sea"), ("山", "the mountain")]),
            L("旅行の買い物", "旅行の買い物", [("お土産", "a souvenir"), ("水", "water"), ("アイスクリーム", "an ice cream")]),
            L("復習", "復習", [("旅行", "the trip"), ("ホテル", "the hotel"), ("美しい", "beautiful")]),
        ],
    ),
    (
        "自由時間",
        [
            L("趣味", "趣味", [("読む", "reading"), ("描く", "painting"), ("料理する", "cooking")]),
            L("音楽", "音楽", [("音楽", "music"), ("ロック", "rock"), ("ポップ", "pop")]),
            L("映画", "映画", [("映画", "a film"), ("ドラマ", "a series"), ("ドキュメンタリー", "a documentary")]),
            L("スポーツ", "スポーツ", [("サッカー", "football"), ("テニス", "tennis"), ("バスケットボール", "basketball")]),
            L("誘い", "誘い", [("出かけ", "go out"), ("夕食を食べ", "have dinner"), ("映画を見", "watch a film")]),
            L("町", "町", [("映画館", "the cinema"), ("博物館", "the museum"), ("レストラン", "the restaurant")]),
            L("好み", "好み", [("音楽", "music"), ("スポーツ", "sport"), ("読書", "reading")]),
            L("楽器", "楽器", [("弾く", "play"), ("歌う", "sing"), ("踊る", "dance")]),
            L("週末", "週末", [("休みます", "rest"), ("出かけます", "go out"), ("本 を 読みます", "read")]),
            L("復習", "復習", [("本", "the book"), ("ゲーム", "the game"), ("面白い", "fun")]),
        ],
    ),
    (
        "自然",
        [
            L("動物", "動物", [("馬", "a horse"), ("牛", "a cow"), ("羊", "a sheep")]),
            L("野生動物", "野生動物", [("ライオン", "a lion"), ("象", "an elephant"), ("猿", "a monkey")]),
            L("植物", "植物", [("花", "a flower"), ("木", "a tree"), ("植物", "a plant")]),
            L("天気", "天気", [("晴れ", "sunny"), ("雨", "rainy"), ("曇り", "cloudy")]),
            L("季節", "季節", [("春", "spring"), ("夏", "summer"), ("秋", "autumn")]),
            L("田舎", "田舎", [("田舎", "the countryside"), ("川", "the river"), ("森", "the forest")]),
            L("山", "山", [("山", "the mountain"), ("湖", "the lake"), ("小道", "the path")]),
            L("行動", "行動", [("歩く", "walk"), ("泳ぐ", "swim"), ("走る", "run")]),
            L("環境", "環境", [("リサイクルする", "recycle"), ("節約する", "save"), ("守る", "protect")]),
            L("復習", "復習", [("川", "the river"), ("公園", "the park"), ("美しい", "beautiful")]),
        ],
    ),
    (
        "技術",
        [
            L("携帯電話", "携帯電話", [("携帯電話", "a phone"), ("タブレット", "a tablet"), ("充電器", "a charger")]),
            L("コンピューター", "コンピューター", [("コンピューター", "a computer"), ("キーボード", "a keyboard"), ("画面", "a screen")]),
            L("インターネット", "インターネット", [("閲覧する", "browse"), ("ダウンロードする", "download"), ("検索する", "search")]),
            L("メッセージ", "メッセージ", [("メッセージ", "a message"), ("写真", "a photo"), ("メール", "an email")]),
            L("写真", "写真", [("撮る", "take"), ("見る", "watch"), ("聞く", "listen")]),
            L("アプリ", "アプリ", [("アプリ", "an app"), ("アカウント", "an account"), ("パスワード", "a password")]),
            L("安全", "安全", [("パスワード", "a password"), ("助け", "help"), ("プライバシー", "privacy")]),
            L("ネットワーク", "ネットワーク", [("ネットワーク", "the network"), ("クラウド", "the cloud"), ("ウェブサイト", "the website")]),
            L("問題", "問題", [("問題", "a problem"), ("質問", "a question"), ("解決策", "a solution")]),
            L("復習", "復習", [("携帯電話", "the phone"), ("コンピューター", "the computer"), ("新しい", "new")]),
        ],
    ),
    (
        "文化",
        [
            L("祭り", "祭り", [("お誕生日 おめでとう", "happy birthday"), ("明けまして おめでとう", "happy new year"), ("よい 休み を", "happy holidays")]),
            L("誕生日", "誕生日", [("ケーキ", "a cake"), ("贈り物", "a gift"), ("カード", "a card")]),
            L("伝統", "伝統", [("音楽", "music"), ("食べ物", "food"), ("家族", "family")]),
            L("誘い", "誘い", [("パーティー に 来", "come to the party"), ("贈り物 を 持って 来", "bring a gift"), ("一緒 に 祝い", "celebrate together")]),
            L("音楽", "音楽", [("伝統 音楽", "traditional music"), ("フラメンコ", "flamenco"), ("タンゴ", "tango")]),
            L("芸術", "芸術", [("博物館", "the museum"), ("ギャラリー", "the gallery"), ("劇場", "the theatre")]),
            L("名物", "名物", [("パエリア", "a paella"), ("タコス", "a taco"), ("アレパ", "an arepa")]),
            L("贈り物", "贈り物", [("花", "flowers"), ("チョコレート", "chocolate"), ("本", "a book")]),
            L("映画", "映画", [("映画", "a film"), ("ドキュメンタリー", "a documentary"), ("コメディ", "a comedy")]),
            L("復習", "復習", [("祭り", "the festival"), ("コンサート", "the concert"), ("人気", "popular")]),
        ],
    ),
    (
        "復習とプロジェクト",
        [
            L("日課の復習", "日課", [("七時 に 起きます", "wake up at seven"), ("働きます", "work"), ("十時 に 寝ます", "go to bed at ten")]),
            L("食べ物の復習", "食べ物", [("りんご", "an apple"), ("ご飯", "rice"), ("鶏肉", "chicken")]),
            L("場所の復習", "場所", [("公園", "the park"), ("学校", "the school"), ("スーパー", "the supermarket")]),
            L("買い物の復習", "買い物", [("シャツ", "a shirt"), ("パン", "bread"), ("贈り物", "a gift")]),
            L("旅行の復習", "旅行", [("電車", "by train"), ("飛行機", "by plane"), ("車", "by car")]),
            L("計画", "計画", [("旅行し", "travel"), ("勉強し", "study"), ("働き", "work")]),
            L("誘い", "誘い", [("出かけ", "go out"), ("夕食を食べ", "have dinner"), ("映画を見", "watch a film")]),
            L("説明", "説明", [("プロジェクト", "the project"), ("計画", "the plan"), ("面白い", "interesting")]),
            L("問題と解決", "問題", [("問題", "a problem"), ("質問", "a question"), ("解決策", "a solution")]),
            L("評価", "復習", [("本", "a book"), ("リュック", "a backpack"), ("ペン", "a pen")]),
        ],
    ),
    (
        "A1 のまとめ",
        [
            L("自己紹介", "自己紹介", [("私 は アナ です", "My name is Ana"), ("スペイン から 来ました", "I am from Spain"), ("二十歳 です", "I am twenty years old")]),
            L("家族", "家族", [("兄 が います", "I have a brother"), ("母 は 先生 です", "My mother is a teacher"), ("父 は 働きます", "My father works")]),
            L("日課", "日課", [("七時 に 起きます", "I wake up at seven"), ("八時 に 朝ごはん を 食べます", "I have breakfast at eight"), ("十時 に 寝ます", "I go to bed at ten")]),
            L("家", "家", [("アパート に 住んで います", "I live in a flat"), ("部屋 が 三つ あります", "There are three rooms"), ("台所 は 小さい です", "My kitchen is small")]),
            L("町", "町", [("マドリード に 住んで います", "I live in Madrid"), ("公園 が あります", "There is a park"), ("交通 は 便利 です", "The transport is good")]),
            L("食べ物", "食べ物", [("ピザ が 好き です", "I like pizza"), ("果物 を 食べます", "I eat fruit"), ("水 を 飲みます", "I drink water")]),
            L("仕事", "仕事", [("オフィス で 働いて います", "I work in an office"), ("上司 は 親切 です", "My boss is nice"), ("会議 が あります", "I have a meeting")]),
            L("旅行", "旅行", [("電車 で 旅行します", "I travel by train"), ("ホテル は 美しい です", "The hotel is nice"), ("スーツケース が あります", "I have a suitcase")]),
            L("自由時間", "自由時間", [("読む こと が 好き です", "I like reading"), ("映画 を 見ます", "I watch a film"), ("友達 と 出かけます", "I go out with friends")]),
            L("最終評価", "復習", [("英語 を 話します", "I speak English"), ("ポルトガル語 を 勉強します", "I learn Portuguese"), ("旅行したい です", "I want to travel")]),
        ],
    ),
]


def tokens(text: str) -> list[str]:
    return [token for token in text.split() if token]


def ja_sentences(pattern: str, items: list[tuple[str, str]]) -> list[str]:
    ja = [item[1] for item in items]
    if pattern == "say":
        return [f"{ja[0]} と 言います", f"{ja[1]} と 言います", f"{ja[2]} と 言います"]
    if pattern == "number":
        return [f"私 は 本 を {ja[0]} 持って います", f"私 は 本 を {ja[1]} 持って います", f"私 は 本 を {ja[2]} 持って います"]
    if pattern == "age":
        return [f"私 は {ja[0]} 歳 です", f"私 は {ja[1]} 歳 です", f"私 は {ja[2]} 歳 です"]
    if pattern == "color":
        return [f"車 は {ja[0]} です", f"車 は {ja[1]} です", f"車 は {ja[2]} です"]
    if pattern == "classroom":
        return [f"これ は {ja[0]} です", f"これ は {ja[1]} です", f"これ は {ja[2]} です"]
    if pattern == "family":
        return [f"私 は {ja[0]} が います", f"私 は {ja[1]} が います", f"私 は {ja[2]} が います"]
    if pattern == "job":
        return [f"私 は {ja[0]} です", f"私 は {ja[1]} です", f"私 は {ja[2]} です"]
    if pattern == "have":
        return [f"私 は {ja[0]} を 飼って います", f"私 は {ja[1]} が 欲しい です", f"私 は {ja[2]} が 必要 です"]
    if pattern == "like":
        return [f"私 は {ja[0]} が 好き です", f"私 は {ja[1]} が 好き です", f"私 は {ja[2]} が 好き です"]
    if pattern == "place":
        return [f"私 は {ja[0]} に います", f"私 は {ja[1]} に います", f"私 は {ja[2]} に います"]
    if pattern == "adjective":
        return [f"{ja[0]} は {ja[2]} です", f"{ja[1]} は {ja[2]} です", f"私 は {ja[0]} が 好き です"]
    if pattern == "eat":
        return [f"私 は {ja[0]} を 食べます", f"私 は {ja[1]} を 食べます", f"私 は {ja[2]} を 食べます"]
    if pattern == "drink":
        return [f"私 は {ja[0]} を 飲みます", f"私 は {ja[1]} を 飲みます", f"私 は {ja[2]} を 飲みます"]
    if pattern == "want":
        return [f"私 は {ja[0]} が 欲しい です", f"私 は {ja[1]} が 欲しい です", f"私 は {ja[2]} が 欲しい です"]
    if pattern == "need":
        return [f"私 は {ja[0]} が 必要 です", f"私 は {ja[1]} が 必要 です", f"私 は {ja[2]} が 必要 です"]
    if pattern == "buy":
        return [f"私 は {ja[0]} を 買います", f"私 は {ja[1]} を 買います", f"私 は {ja[2]} を 買います"]
    if pattern == "see":
        return [f"私 は {ja[0]} を 見ます", f"私 は {ja[1]} を 見ます", f"私 は {ja[2]} を 見ます"]
    if pattern == "verb":
        return [f"私 は {ja[0]} こと が できます", f"私 は {ja[1]} こと が したい です", f"私 は {ja[2]} こと が 好き です"]
    if pattern == "hobby":
        return [f"私 は {ja[0]} こと が 好き です", f"私 は {ja[1]} こと が 好き です", f"私 は {ja[2]} こと が 好き です"]
    if pattern == "routine":
        return [f"私 は {ja[0]}", f"私 は {ja[1]}", f"私 は {ja[2]}"]
    if pattern == "health":
        return [f"私 は {ja[0]}", f"私 は {ja[1]}", f"私 は {ja[2]}"]
    if pattern == "body":
        return [f"{ja[0]} を 触って ください", f"{ja[1]} を 触って ください", f"{ja[2]} を 触って ください"]
    if pattern == "transport":
        return [f"{ja[0]} で 行きます", f"{ja[1]} で 行きます", f"{ja[2]} で 行きます"]
    if pattern == "direction":
        return [f"{ja[0]} へ 行って ください", f"{ja[1]} へ 行って ください", f"{ja[2]} へ 行って ください"]
    if pattern == "sport":
        return [f"{ja[0]} を します", f"{ja[1]} を します", f"{ja[2]} を します"]
    if pattern == "music":
        return [f"{ja[0]} を 聞きます", f"{ja[1]} を 聞きます", f"{ja[2]} を 聞きます"]
    if pattern == "movie":
        return [f"{ja[0]} を 見ます", f"{ja[1]} を 見ます", f"{ja[2]} を 見ます"]
    if pattern == "invitation":
        return [f"{ja[0]} ません か", f"{ja[1]} ません か", f"{ja[2]} ません か"]
    if pattern == "time":
        return [f"今 {ja[0]} です", f"今 {ja[1]} です", f"今 {ja[2]} です"]
    if pattern == "price":
        return [f"{ja[0]} です", f"{ja[1]} です", f"{ja[2]} です"]
    if pattern == "weather":
        return [f"今日 は {ja[0]} です", f"今日 は {ja[1]} です", f"今日 は {ja[2]} です"]
    if pattern == "weather2":
        return [f"今日 は {ja[0]} です", f"今日 は {ja[1]} です", f"今日 は {ja[2]} です"]
    if pattern == "season":
        return [f"{ja[0]} は いい 天気 です", f"{ja[1]} は いい 天気 です", f"{ja[2]} は いい 天気 です"]
    if pattern == "message":
        return [f"私 は {ja[0]} を 送ります", f"私 は {ja[1]} を 送ります", f"私 は {ja[2]} を 送ります"]
    if pattern == "problem":
        return [f"私 は {ja[0]} が あります", f"私 は {ja[1]} が あります", f"私 は {ja[2]} が あります"]
    if pattern == "want_verb":
        return [f"私 は {ja[0]} たい です", f"私 は {ja[1]} たい です", f"私 は {ja[2]} たい です"]
    if pattern == "weekend":
        return [f"土曜日 に {ja[0]}", f"土曜日 に {ja[1]}", f"土曜日 に {ja[2]}"]
    if pattern == "phrase":
        return [ja[0], ja[1], ja[2]]
    raise ValueError(f"Patrón desconocido: {pattern}")


def safe_distractors(correct: list[str], candidates: list[list[str]]) -> list[str]:
    pool: list[str] = []
    for candidate in candidates:
        for word in candidate:
            if word not in correct and word not in pool:
                pool.append(word)
    for fallback in ["とても", "今日", "ここ", "今", "も", "いつも"]:
        if len(pool) >= 3:
            break
        if fallback not in correct and fallback not in pool:
            pool.append(fallback)
    return pool[:3]


def build_lesson(block: int, index: int, spec: dict) -> dict:
    lesson_id = f"{COURSE_ID}-a1-u{block:02d}-l{index:02d}"
    es_spec = F1_1.BLOCKS[block - 1][1][index - 1]
    pattern = es_spec["pattern"]
    es_items = es_spec["items"]
    ja_only = [item[0] for item in spec["items"]]
    if len(ja_only) != len(es_items):
        raise SystemExit(f"{lesson_id}: número de ítems japonés/español distinto")
    ja_items = [(es, ja) for (es, _en), ja in zip(es_items, ja_only)]
    if pattern == "weather2":
        es_sentences = ["Hoy hace sol", "Hoy llueve", "Hoy está nublado"]
        ja_sentences_text = ja_sentences("weather", ja_items)
    else:
        es_pairs = F1_1.sentences_for(pattern, es_items)
        es_sentences = [pair[0] for pair in es_pairs]
        ja_sentences_text = ja_sentences(pattern, ja_items)
    ja_tokens = [tokens(text) for text in ja_sentences_text]
    first_es, first_ja = ja_items[0]
    return {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "A1",
        "vocabFrequencyBand": "1000-2000",
        "canDo": f"Puedo usar frases sencillas sobre {spec['topic']} en japonés.",
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Reconocer vocabulario de {spec['topic']}.",
                f"Usar frases sencillas sobre {spec['topic']} en japonés.",
            ],
            "sections": [
                {
                    "heading": "言葉と文",
                    "body": f"Vocabulario básico de {spec['topic']}. Fíjate en el orden de la frase y en las partículas.",
                    "examples": [
                        {"source": es_sentences[0], "target": ja_sentences_text[0]},
                        {"source": es_sentences[1], "target": ja_sentences_text[1]},
                    ],
                }
            ],
        },
        "glossary": [{"term": ja, "translations": [es]} for es, ja in ja_items],
        "exercises": [
            {
                "type": "MultipleChoice",
                "id": f"{lesson_id}-e01",
                "question": f"¿Cómo se dice «{first_es}» en japonés?",
                "options": [ja for _, ja in ja_items],
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": f"«{first_es}» se dice «{first_ja}».",
                },
                "feedbackOnSuccess": {"explanation": "Has elegido la traducción correcta."},
            },
            {
                "type": "Translate",
                "id": f"{lesson_id}-e02",
                "sourcePhrase": es_sentences[0],
                "correctTokens": ja_tokens[0],
                "distractorTokens": safe_distractors(ja_tokens[0], [ja_tokens[1], ja_tokens[2]]),
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"El orden correcto es «{' '.join(ja_tokens[0])}».",
                },
                "feedbackOnSuccess": {"explanation": "La frase conserva el orden y el significado."},
            },
            {
                "type": "OrderWords",
                "id": f"{lesson_id}-e03",
                "tokens": (ja_tokens[1][1:] + ja_tokens[1][:1]) if len(ja_tokens[1]) > 1 else ja_tokens[1],
                "answerTokens": ja_tokens[1],
                "sourcePhrase": es_sentences[1],
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"La frase correcta es «{' '.join(ja_tokens[1])}».",
                },
                "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
            },
            {
                "type": "MatchPairs",
                "id": f"{lesson_id}-e04",
                "pairs": [{"left": es, "right": ja} for es, ja in ja_items],
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": "Cada palabra tiene una traducción distinta.",
                },
                "feedbackOnSuccess": {"explanation": "Has asociado cada palabra con su traducción."},
            },
            {
                "type": "ListenChoose",
                "id": f"{lesson_id}-e05",
                "audioText": ja_sentences_text[2],
                "options": ja_sentences_text,
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "comprehension",
                    "explanation": f"El audio dice «{ja_sentences_text[2]}».",
                },
                "feedbackOnSuccess": {"explanation": "Has reconocido la frase del audio."},
            },
            {
                "type": "ListenType",
                "id": f"{lesson_id}-e06",
                "audioText": ja_sentences_text[2],
                "transcription": ja_sentences_text[2],
                "feedbackOnError": {
                    "type": "spelling",
                    "explanation": f"Escribe exactamente «{ja_sentences_text[2]}».",
                },
                "feedbackOnSuccess": {"explanation": "La transcripción coincide con el audio."},
            },
            {
                "type": "SpeakRepeat",
                "id": f"{lesson_id}-e07",
                "targetPhrase": ja_sentences_text[2],
                "audioText": ja_sentences_text[2],
                "feedbackOnError": {
                    "type": "pronunciation",
                    "explanation": f"Repite «{ja_sentences_text[2]}» con claridad.",
                },
                "feedbackOnSuccess": {"explanation": "La frase se entiende y mantiene el ritmo."},
            },
        ],
    }


def self_check(lessons: list[dict]) -> None:
    assert len(lessons) == 150, f"se esperaban 150 lecciones, hay {len(lessons)}"
    seen_titles = set()
    for lesson in lessons:
        assert len(lesson["exercises"]) == 7, f"{lesson['id']}: se esperaban 7 ejercicios"
        assert lesson.get("grammarPoints") is None, f"{lesson['id']}: A1 no declara gramática"
        assert lesson["title"] not in seen_titles, f"título duplicado: {lesson['title']}"
        seen_titles.add(lesson["title"])
        for exercise in lesson["exercises"]:
            texts: list[str] = []
            if exercise["type"] == "Translate":
                texts.append(" ".join(exercise["correctTokens"]))
            elif exercise["type"] == "OrderWords":
                texts.append(" ".join(exercise["answerTokens"]))
            elif exercise["type"] in {"ListenType", "SpeakRepeat", "ListenChoose"}:
                texts.append(exercise["audioText"])
            for text in texts:
                assert len(text.split()) <= 8, f"{lesson['id']}/{exercise['id']}: frase larga ({text})"
            if exercise["type"] == "Translate":
                overlap = set(exercise["correctTokens"]).intersection(exercise["distractorTokens"])
                assert not overlap, f"{lesson['id']}: distractor repetido {overlap}"


def find_insert_bracket(text: str) -> int:
    level_match = re.search(r'\n[ \t]*\{\n[ \t]*"id": "' + re.escape(NEXT_LEVEL) + '"', text)
    if level_match is None:
        raise SystemExit(f"No se encontró el nivel {NEXT_LEVEL}")
    bracket = None
    for match in re.finditer(r"\n([ \t]*)\]", text[: level_match.start()]):
        bracket = match
    if bracket is None:
        raise SystemExit("No se encontró el cierre de units de A1")
    return bracket.start(), len(bracket.group(1)) + 2


def indent_block(text: str, spaces: int) -> str:
    prefix = " " * spaces
    return "\n".join(prefix + line for line in text.splitlines())


def main() -> None:
    text = COURSE.read_text(encoding="utf-8")
    if f"{COURSE_ID}-a1-u01" in text:
        print("F1.4 ya existe; no se modifica.")
        return
    lessons = []
    units = []
    for block_number, (block_title, block_lessons) in enumerate(JA_BLOCKS, start=1):
        block = [
            build_lesson(block_number, lesson_number, spec)
            for lesson_number, spec in enumerate(block_lessons, start=1)
        ]
        lessons.extend(block)
        units.append(
            {
                "id": f"{COURSE_ID}-a1-u{block_number:02d}",
                "title": f"A1 · {block_title}",
                "lessons": block,
            }
        )
    used_titles: set[str] = set()
    for unit in units:
        block_title = unit["title"].removeprefix("A1 · ")
        for lesson in unit["lessons"]:
            base = lesson["title"]
            title = base
            if title in used_titles:
                title = f"{base} · {block_title}"
                counter = 2
                while title in used_titles:
                    title = f"{base} · {block_title} {counter}"
                    counter += 1
            lesson["title"] = title
            lesson["microLesson"]["title"] = title
            used_titles.add(title)
    self_check(lessons)
    bracket_start, indent = find_insert_bracket(text)
    units_text = ",\n".join(
        indent_block(json.dumps(unit, ensure_ascii=False, indent=2), indent) for unit in units
    )
    text = text[:bracket_start] + ",\n" + units_text + text[bracket_start:]
    COURSE.write_text(text, encoding="utf-8", newline="\n")
    json.loads(COURSE.read_text(encoding="utf-8"))
    print(f"Insertados 15 bloques con {len(lessons)} lecciones y {len(lessons) * 7} ejercicios.")


if __name__ == "__main__":
    main()
