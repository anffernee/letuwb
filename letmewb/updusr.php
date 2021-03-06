<?php
include "extrakits.inc.php";
include 'zmysqlConn.class.php';
include_once("weibo/expkits.inc.php");
$zconn = new zmysqlConn;
/*
 * the script takes 3 parameters:user, channelid, clientkey.
 * and it'll update or insert one record into table users according them:
 * if the clientkey from table users according to "user[id], channelid" is 
 * different with the one passed from "GET", do nothing to table users but 
 * just return the clientkey from table users. //and at the same time set
 * the flag of the one passed from "GET" in table clients into -1. 
 */
require_once './protocolbuf/message/pb_message.php';
require_once './protocolbuf/parser/pb_proto_weibousers_protos.php';
$mappings = new UCMappings();
if (!array_key_exists('user', $_GET)
	|| !array_key_exists('channelid', $_GET)
	|| !array_key_exists('clientkey', $_GET)) {
	$mappings->set_flag(0);//0 means parameters are kind of wrong or something
	echo $mappings->SerializeToString();
	exit();
}
$user = $_GET['user'];
if (!is_array($user)) {
	$mappings->set_flag(0);//0 means parameters are kind of wrong or something
	echo $mappings->SerializeToString();
	exit();
}

$profile_updated = false;
$sql = __get_user_insert_update_sql($user);
mysql_query("set names 'utf8';");
$rs = mysql_query($sql, $zconn->dblink);
if ($rs === false) {
	$profile_updated = false;
} else {
	$profile_updated = true;
}

$uid = $user['id'];
$channelid = $_GET['channelid'];
$clientkey = $_GET['clientkey'];
$sql = sprintf("select * from clients where `key` = '%s'", $clientkey);
$rs = mysql_query($sql, $zconn->dblink);
if ($rs === false) {
	$mappings->set_flag(-1);//-1 means there is some db related errors
	echo $mappings->SerializeToString();
	exit();
}
$clientid = 0;
if (mysql_num_rows($rs) == 0) {
	$mappings->set_flag(-2);//-2 means illegal clientkey
	echo $mappings->SerializeToString();
	exit();
} else {
	$r = mysql_fetch_assoc($rs);
	$clientid = $r['id'];
}
$sql = sprintf(
	"select users.*, clients.key as clientkey from users, clients where users.uid = %s and users.channelid = %s and users.clientid = clients.id", 
	$uid, $channelid
);
$rs = mysql_query($sql, $zconn->dblink);
if ($rs === false) {
	$mappings->set_flag(-1);//-1 means there is some db related errors
	echo $mappings->SerializeToString();
	exit();
}

if (mysql_num_rows($rs) == 0) {
	$sql = sprintf(
		"insert into users (uid, channelid, clientid) values (%s, %s, %s)",
		$uid, $channelid, $clientid
	);
	mysql_query($sql, $zconn->dblink);
	if (mysql_insert_id() == 0 || mysql_insert_id() === false) {
		$mappings->set_flag(-1);//-1 means there is some db related errors
		echo $mappings->SerializeToString();
		exit();
	} else {
		$mappings->set_flag(($profile_updated ? 10 : 0) + 1);//1 means successfully insert a record
		$mapping = $mappings->add_mapping();
		$mapping->set_id(mysql_insert_id());
		$mapping->set_uid($uid . '');
		$mapping->set_channelid($channelid . '');
		$mapping->set_clientid($clientid . '');
		$mapping->set_clientkey($clientkey);
		echo $mappings->SerializeToString();
		exit();
	}
} else {
	$r = mysql_fetch_assoc($rs);
	if ($r['clientkey'] == $clientkey) {
		$mappings->set_flag(($profile_updated ? 10 : 0) + 2);//2 means all 3 do already exist in table users
		echo $mappings->SerializeToString();
		exit();
	} else {
		//we should merge the possessions in the old clientkey into the new one's
		$sql = sprintf(
			"select concat(cast(uid as char), \",\", cast(channelid as char))"
			. " from possessions where clientid = %s",
			$r['clientid']
		);
		//echo $sql . "<br/>";//for debug
		$uids = "''";
		$rs = mysql_query($sql, $zconn->dblink);
		if ($rs === false) {
			$mappings->set_flag(-1);//-1 means there is some db related errors
			echo $mappings->SerializeToString();
			exit();
		}
		while ($row = mysql_fetch_row($rs)) {
			$uids .= (", '" . $row[0] . "'");
		}
		//echo $uids . "<br/>";//for debug
		$sql = sprintf(
			"delete from possessions where clientid = %s"
			. " and concat(cast(uid as char), \",\", cast(channelid as char))"
			. " in (%s)",
			$clientid, $uids
		);
		//echo $sql . "<br/>";//for debug
		mysql_query($sql, $zconn->dblink);
		$sql = sprintf(
			"update possessions set clientid = %s where clientid = %s",
			$r['clientid'], $clientid
		);
		//echo $sql . "<br/>";//for debug
		mysql_query($sql, $zconn->dblink);
		$mappings->set_flag(($profile_updated ? 10 : 0) + 3);//3 means the client should replace his key with the returned one
		$mapping = $mappings->add_mapping();
		$mapping->set_id($r['id'] . '');
		$mapping->set_uid($r['uid'] . '');
		$mapping->set_channelid($r['channelid'] . '');
		$mapping->set_clientid($r['clientid'] . '');
		$mapping->set_clientkey($r['clientkey']);
		echo $mappings->SerializeToString();
		exit();
	}	
}
?>