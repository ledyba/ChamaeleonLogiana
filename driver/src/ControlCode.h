/*
 * EzUSB.h
 *
 *  Created on: Mar 12, 2013
 *      Author: psi
 */

#pragma once
#include <usb.h>

#define	GPFW_CPIPE	(USB_ENDPOINT_OUT|1)//0		//コマンド用パイプ(1IN)への番号
#define	GPFW_WPIPE	(USB_ENDPOINT_OUT|2)//1		//ライトデータパイプ(2IN)への番号
#define	GPFW_RPIPE	(USB_ENDPOINT_IN|2)//8		//リードデータパイプ(2OUT)への番号

#define	GPFW_DIR	0x00	//EZ-USBのポートの入力・出力を設定。nnnnの各bitで0:入力　1:出力
#define	GPFW_SET	0x10	//ポートに値を出力する。nnnnが各ポートの出力値
#define	GPFW_GET	0x20	//ポートの値を読み込む。nnnnは無視され、ポートの入力値がINパケットに送られる
#define	GPFW_WRITE	0x30	//nnnnをポートに書きこみ、後続の1byteをDATA BUSにFirstWriteする (2byteコマンド)
#define	GPFW_READ	0x40	//nnnnをポートに書きこみ、DATA BUSからFirstReadしてINパケットに送る
#define	GPFW_BWRITE	0x50	//nnnnをポートに書きこみ、
							//(次の 1byte & 0x3f) + ((次の次の 1byte)<<6) + ((次の次の次の 1byte) << 14)
							//の長さをエンドポイントから読み込み、DATA BUSにFirstWriteする (4byteコマンド)
#define	GPFW_BREAD	0x60	//nnnnをポートに書きこみ、
							//(次の 1byte & 0x3f) + ((次の次の 1byte)<<6) + ((次の次の次の 1byte) << 14)
							//byteをDATA BUSからFirstReadして、 INパケットでＰＣに送る(4byteコマンド)
#define	GPFW_BUS		0xf0	//nnnnが0ならstrechを000、0以外なら001に設定 (2byteコマンド)
							//後続の1byteをFASTXFRにセット（上位2bitはファームがコントロール）

#define	LGFW_GETBAD	0x70	//無効データの長さを取得
