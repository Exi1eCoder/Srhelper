package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.dto.SrCharacterSaveDTO;
import com.quico.srhelper.domain.vo.CelestialCharacterVO;
import com.quico.srhelper.domain.vo.LimitedCharacterOptionVO;
import com.quico.srhelper.domain.vo.SrCharacterDetailVO;

/**
 * 角色Service接口
 * 
 * @author quico
 * @date 2026-05-12
 */
public interface ISrCharacterService 
{
    /**
     * 查询角色
     * 
     * @param id 角色主键
     * @return 角色
     */
    public SrCharacter selectSrCharacterById(Long id);

    /**
     * 查询角色列表
     * 
     * @param srCharacter 角色
     * @return 角色集合
     */
    public List<SrCharacter> selectSrCharacterList(SrCharacter srCharacter);

    /**
     * 新增角色
     * 
     * @param srCharacter 角色
     * @return 结果
     */
    public int insertSrCharacter(SrCharacter srCharacter);

    /**
     * 修改角色
     * 
     * @param srCharacter 角色
     * @return 结果
     */
    public int updateSrCharacter(SrCharacter srCharacter);

    /**
     * 批量加入星缘相遇
     * 版本联动强校验：按版本号查询实装时间，版本不存在或未配置实装时间时抛出业务异常
     *
     * @param ids 角色ID集合
     * @param versionCode 版本号（必填，如 3.2）
     * @return 更新行数
     */
    int addToCelestialEncounter(Long[] ids, String versionCode);

    /**
     * 查询已加入星缘相遇的角色列表（SQL 强制 standard_type = 2）
     *
     * @param characterName 角色名字（可选模糊匹配）
     * @return 星缘相遇角色精简视图
     */
    List<CelestialCharacterVO> selectCelestialCharacterList(String characterName);

    /**
     * 查询可作为星缘相遇候选的限定角色（SQL 强制 standard_type = 0）
     *
     * @param characterName 角色名字（可选模糊匹配）
     * @return 限定角色下拉选项
     */
    List<LimitedCharacterOptionVO> selectLimitedCharacterOptions(String characterName);

    /**
     * 批量删除角色
     * 
     * @param ids 需要删除的角色主键集合
     * @return 结果
     */
    public int deleteSrCharacterByIds(Long[] ids);

    /**
     * 删除角色信息
     * 
     * @param id 角色主键
     * @return 结果
     */
    public int deleteSrCharacterById(Long id);

    /**
     * 统一保存/更新角色（包含所有关联数据）
     * 
     * @param dto 角色保存DTO
     * @return 角色ID
     */
    Long saveAll(SrCharacterSaveDTO dto);

    /**
     * 查询角色完整信息（包含所有关联数据）
     * 
     * @param id 角色ID
     * @return 角色详细信息
     */
    SrCharacterDetailVO getDetail(Long id);

    /**
     * 删除角色（包含所有关联数据）
     * 
     * @param id 角色ID
     */
    void deleteCharacter(Long id);

    /**
     * 批量保存角色（包含所有关联数据）
     *
     * @param dtoList 角色保存DTO列表
     * @return 角色ID列表
     */
    List<Long> saveBatch(List<SrCharacterSaveDTO> dtoList);

    /**
     * 导入角色数据（按角色名字 + 实装版本去重）
     *
     * @param list 从 Excel 解析的角色列表
     * @param updateSupport 已存在时是否更新（false=跳过）
     * @param operName 操作人ID
     * @return 导入结果消息
     */
    String importCharacter(List<SrCharacter> list, boolean updateSupport, String operName);
}
