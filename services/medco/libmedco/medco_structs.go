package libmedco

import (
	"github.com/dedis/cothority/sda"
	"github.com/dedis/crypto/abstract"
	"strings"
)

const MAX_GROUP_ATTR int = 2
const PROOF = false

type GroupingAttributes DeterministCipherVector
type GroupingKey string
type TempID uint64

type ClientResponse struct {
	ProbabilisticGroupingAttributes CipherVector
	AggregatingAttributes           CipherVector
}

type SurveyID string

type Survey struct {
	*SurveyStore
	ID                SurveyID
	Roster            sda.Roster
	SurveyPHKey       abstract.Scalar
	ClientPublic      abstract.Point
	SurveyDescription SurveyDescription
}

type SurveyDescription struct {
	GroupingAttributesCount    int32
	AggregatingAttributesCount uint32
}

func (ga *GroupingAttributes) Key() GroupingKey {
	var key []string
	for _, a := range DeterministCipherVector(*ga) {
		key = append(key, a.String())
	}
	return GroupingKey(strings.Join(key, ""))
}

func (ga *GroupingAttributes) Equal(ga2 *GroupingAttributes) bool {
	if ga == nil || ga2 == nil {
		return ga == ga2
	}
	for i, v := range DeterministCipherVector(*ga) {
		temp := (*ga2)[i]
		if !v.Equal(&temp) {
			return false
		}
	}
	return true
}

func GroupingAttributesToDeterministicCipherVector(ga *map[TempID]GroupingAttributes) *map[TempID]DeterministCipherVector {
	deterministicCipherVector := make(map[TempID]DeterministCipherVector, len(*ga))
	for k := range *ga {
		deterministicCipherVector[k] = DeterministCipherVector((*ga)[k])
	}
	return &deterministicCipherVector
}

func DeterministicCipherVectorToGroupingAttributes(dcv *map[TempID]DeterministCipherVector) *map[TempID]GroupingAttributes {
	deterministicGroupAttributes := make(map[TempID]GroupingAttributes, len(*dcv))
	for k := range *dcv {
		deterministicGroupAttributes[k] = GroupingAttributes((*dcv)[k])
	}
	return &deterministicGroupAttributes
}
