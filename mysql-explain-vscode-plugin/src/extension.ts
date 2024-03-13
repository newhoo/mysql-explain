import * as vscode from 'vscode';
import { existsSync } from 'fs';
import * as path from 'path';
import { parse, stringify, CommentArray, CommentObject, CommentJSONValue } from 'comment-json';

export function activate(context: vscode.ExtensionContext) {
	const openGitInBrowserCommand = vscode.commands.registerCommand("java-mysql-explain.GenarateMysqlExplainVmargs", (arg: any) => {
		const workspaceFolders = vscode.workspace.workspaceFolders;
		if (workspaceFolders == undefined || workspaceFolders.length < 1) {
			return
		}
		const extensionPath = vscode.extensions.getExtension("newhoo.java-mysql-explain")?.extensionPath
		const agentPath = extensionPath + path.sep + "jars" + path.sep + "mysql-explain-agent.jar"
		if (!existsSync(agentPath)) {
			return
		}
		const activeTextEditor = vscode.window.activeTextEditor
		if (!activeTextEditor || activeTextEditor.document.fileName.indexOf('launch.json') < 0) {
			vscode.window.showInformationMessage("You can do this only in current editor whith `launch.json` file.");
			return
		}

		const mysqlExplainConfig = vscode.workspace.getConfiguration("java-mysql-explain")

		const launchObj = <CommentObject>parse(activeTextEditor.document.getText())
		if (!launchObj) {
			vscode.window.showErrorMessage("Can't parse this `launch.json` file.");
			return
		}
		const configurations = <CommentArray<CommentObject>>launchObj['configurations']
		let upsertVmArgs = false
		configurations.forEach(configuration => {
			if (configuration['type'] === 'java' && configuration['request'] === 'launch') {
				let vmArgsArr: string[] | CommentArray<string> = []
				if (configuration['vmArgs']) {
					const v = configuration['vmArgs']
					if (typeof v === 'string') {
						const s = v.trim().replaceAll("  ", " ")
						if (s) {
							vmArgsArr = s.split(" ")
						}
					} else if (v instanceof CommentArray) {
						vmArgsArr = <CommentArray<string>>v
					} else {
						return v
					}

					let a, b, c, d, e, f
					vmArgsArr = vmArgsArr.map((s: string) => {
						if (s.startsWith("-javaagent:" + extensionPath)) {
							a = true
							return `-javaagent:${agentPath}`;
						}
						if (s.startsWith("-Dmysql.showSQL=")) {
							b = true
							return `-Dmysql.showSQL=${mysqlExplainConfig.enablePrintSQL}`;
						}
						if (s.startsWith("-Dmysql.showSQL.filter=")) {
							c = true
							return `-Dmysql.showSQL.filter=${mysqlExplainConfig.filterPrintSQL}`;
						}
						if (s.startsWith("-Dmysql.explain.filter=")) {
							d = true
							return `-Dmysql.explain.filter=${mysqlExplainConfig.filterBeforeExplain}`;
						}
						if (s.startsWith("-Dmysql.explain.types=")) {
							e = true
							return `-Dmysql.explain.types=${mysqlExplainConfig.filterByTypeAfterExplain}`;
						}
						if (s.startsWith("-Dmysql.explain.extras=")) {
							f = true
							return `-Dmysql.explain.extras=${mysqlExplainConfig.filterByExtraAfterExplain}`;
						}
						return s
					})
					if (!a) {
						vmArgsArr.push(`-javaagent:${agentPath}`);
					}
					if (!b) {
						vmArgsArr.push(`-Dmysql.showSQL=${mysqlExplainConfig.enablePrintSQL}`);
					}
					if (!c) {
						vmArgsArr.push(`-Dmysql.showSQL.filter=${mysqlExplainConfig.filterPrintSQL}`);
					}
					if (!d) {
						vmArgsArr.push(`-Dmysql.explain.filter=${mysqlExplainConfig.filterBeforeExplain}`);
					}
					if (!e) {
						vmArgsArr.push(`-Dmysql.explain.types=${mysqlExplainConfig.filterByTypeAfterExplain}`);
					}
					if (!f) {
						vmArgsArr.push(`-Dmysql.explain.extras=${mysqlExplainConfig.filterByExtraAfterExplain}`);
					}
				} else {
					vmArgsArr.push(`-javaagent:${agentPath}`);
					vmArgsArr.push(`-Dmysql.showSQL=${mysqlExplainConfig.enablePrintSQL}`);
					vmArgsArr.push(`-Dmysql.showSQL.filter=${mysqlExplainConfig.filterPrintSQL}`);
					vmArgsArr.push(`-Dmysql.explain.filter=${mysqlExplainConfig.filterBeforeExplain}`);
					vmArgsArr.push(`-Dmysql.explain.types=${mysqlExplainConfig.filterByTypeAfterExplain}`);
					vmArgsArr.push(`-Dmysql.explain.extras=${mysqlExplainConfig.filterByExtraAfterExplain}`);
				}
				upsertVmArgs = true
				configuration['vmArgs'] = <CommentJSONValue>vmArgsArr
			}
		})

		if (upsertVmArgs) {
			activeTextEditor.edit(editBuilder => {
				const end = new vscode.Position(activeTextEditor.document.lineCount + 1, 0);
				editBuilder.replace(new vscode.Range(new vscode.Position(0, 0), end), stringify(launchObj, null, 4));
			});
		} else {
			vscode.window.showInformationMessage("Not found or recognize java launch configuration.");
		}
	});

	context.subscriptions.push(openGitInBrowserCommand);
}

export class MysqlExplainConfig {
	enablePrintSQL: boolean
	filterPrintSQL: string
	filterBeforeExplain: string
	filterByTypeAfterExplain: string
	filterByExtraAfterExplain: string
	constructor(enablePrintSQL: boolean, filterPrintSQL: string, filterBeforeExplain: string, filterByTypeAfterExplain: string, filterByExtraAfterExplain: string) {
		this.enablePrintSQL = enablePrintSQL;
		this.filterPrintSQL = filterPrintSQL;
		this.filterBeforeExplain = filterBeforeExplain;
		this.filterByTypeAfterExplain = filterByTypeAfterExplain;
		this.filterByExtraAfterExplain = filterByExtraAfterExplain;
	}
}

export function deactivate() { }
